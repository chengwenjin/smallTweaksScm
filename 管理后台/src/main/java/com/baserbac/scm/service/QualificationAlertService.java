package com.baserbac.scm.service;

import com.baserbac.common.result.PageResult;
import com.baserbac.dto.PageQueryDTO;
import com.baserbac.scm.entity.QualificationAlert;
import com.baserbac.scm.entity.Supplier;
import com.baserbac.scm.entity.SupplierQualification;
import com.baserbac.scm.mapper.QualificationAlertMapper;
import com.baserbac.scm.mapper.SupplierMapper;
import com.baserbac.scm.mapper.SupplierQualificationMapper;
import com.baserbac.scm.vo.QualificationAlertVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QualificationAlertService {

    private final QualificationAlertMapper alertMapper;
    private final SupplierQualificationMapper qualificationMapper;
    private final SupplierMapper supplierMapper;

    private static final int ALERT_DAYS_BEFORE = 30;

    public PageResult<QualificationAlertVO> pageAlerts(PageQueryDTO queryDTO, Integer isRead) {
        Page<QualificationAlert> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<QualificationAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(isRead != null, QualificationAlert::getIsRead, isRead)
               .orderByDesc(QualificationAlert::getCreateTime);
        
        Page<QualificationAlert> result = alertMapper.selectPage(page, wrapper);
        
        List<QualificationAlertVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    public long getUnreadCount() {
        return alertMapper.selectCount(
            new LambdaQueryWrapper<QualificationAlert>()
                .eq(QualificationAlert::getIsRead, 0)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id) {
        QualificationAlert alert = alertMapper.selectById(id);
        if (alert != null && alert.getIsRead() == 0) {
            alert.setIsRead(1);
            alert.setReadTime(LocalDateTime.now());
            alertMapper.updateById(alert);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        List<QualificationAlert> unreadAlerts = alertMapper.selectList(
            new LambdaQueryWrapper<QualificationAlert>()
                .eq(QualificationAlert::getIsRead, 0)
        );
        
        for (QualificationAlert alert : unreadAlerts) {
            alert.setIsRead(1);
            alert.setReadTime(LocalDateTime.now());
            alertMapper.updateById(alert);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkAndCreateAlerts() {
        log.info("开始检查资质到期预警...");
        
        LocalDate today = LocalDate.now();
        LocalDate alertDate = today.plusDays(ALERT_DAYS_BEFORE);
        
        List<SupplierQualification> qualifications = qualificationMapper.selectList(
            new LambdaQueryWrapper<SupplierQualification>()
                .ne(SupplierQualification::getIsLongTerm, 1)
                .isNotNull(SupplierQualification::getExpiryDate)
                .eq(SupplierQualification::getAuditStatus, 2)
                .eq(SupplierQualification::getAlertSent, 0)
                .le(SupplierQualification::getExpiryDate, alertDate)
        );
        
        int alertCount = 0;
        for (SupplierQualification q : qualifications) {
            if (createAlert(q, today)) {
                alertCount++;
                q.setAlertSent(1);
                qualificationMapper.updateById(q);
            }
        }
        
        List<SupplierQualification> expiredQualifications = qualificationMapper.selectList(
            new LambdaQueryWrapper<SupplierQualification>()
                .ne(SupplierQualification::getIsLongTerm, 1)
                .isNotNull(SupplierQualification::getExpiryDate)
                .eq(SupplierQualification::getAuditStatus, 2)
                .eq(SupplierQualification::getAlertStatus, 1)
                .lt(SupplierQualification::getExpiryDate, today)
        );
        
        for (SupplierQualification q : expiredQualifications) {
            q.setAlertStatus(2);
            qualificationMapper.updateById(q);
            
            QualificationAlert existingAlert = alertMapper.selectOne(
                new LambdaQueryWrapper<QualificationAlert>()
                    .eq(QualificationAlert::getQualificationId, q.getId())
                    .eq(QualificationAlert::getAlertType, 2)
            );
            
            if (existingAlert == null) {
                createExpiredAlert(q, today);
                alertCount++;
            }
        }
        
        log.info("资质到期预警检查完成，新增预警数量: {}", alertCount);
    }

    private boolean createAlert(SupplierQualification qualification, LocalDate today) {
        LocalDate expiryDate = qualification.getExpiryDate();
        long daysBeforeExpiry = ChronoUnit.DAYS.between(today, expiryDate);
        
        if (daysBeforeExpiry < 0) {
            return false;
        }
        
        QualificationAlert existingAlert = alertMapper.selectOne(
            new LambdaQueryWrapper<QualificationAlert>()
                .eq(QualificationAlert::getQualificationId, qualification.getId())
                .eq(QualificationAlert::getAlertType, 1)
        );
        
        if (existingAlert != null) {
            return false;
        }
        
        Supplier supplier = supplierMapper.selectById(qualification.getSupplierId());
        String supplierName = supplier != null ? supplier.getSupplierName() : "未知供应商";
        
        QualificationAlert alert = new QualificationAlert();
        alert.setQualificationId(qualification.getId());
        alert.setSupplierId(qualification.getSupplierId());
        alert.setAlertType(1);
        alert.setAlertTitle("资质即将到期提醒");
        alert.setAlertContent(String.format("供应商[%s]的[%s]资质将于%d天后到期，请及时更新。", 
            supplierName, qualification.getQualificationName(), daysBeforeExpiry));
        alert.setAlertDate(today);
        alert.setDaysBeforeExpiry((int) daysBeforeExpiry);
        alert.setIsRead(0);
        
        alertMapper.insert(alert);
        
        log.info("创建资质即将到期预警: 供应商ID={}, 资质ID={}, 到期天数={}", 
            qualification.getSupplierId(), qualification.getId(), daysBeforeExpiry);
        
        return true;
    }

    private void createExpiredAlert(SupplierQualification qualification, LocalDate today) {
        Supplier supplier = supplierMapper.selectById(qualification.getSupplierId());
        String supplierName = supplier != null ? supplier.getSupplierName() : "未知供应商";
        
        QualificationAlert alert = new QualificationAlert();
        alert.setQualificationId(qualification.getId());
        alert.setSupplierId(qualification.getSupplierId());
        alert.setAlertType(2);
        alert.setAlertTitle("资质已过期提醒");
        alert.setAlertContent(String.format("供应商[%s]的[%s]资质已过期，请立即处理。", 
            supplierName, qualification.getQualificationName()));
        alert.setAlertDate(today);
        alert.setDaysBeforeExpiry(0);
        alert.setIsRead(0);
        
        alertMapper.insert(alert);
        
        log.info("创建资质已过期预警: 供应商ID={}, 资质ID={}", 
            qualification.getSupplierId(), qualification.getId());
    }

    private QualificationAlertVO convertToVO(QualificationAlert alert) {
        SupplierQualification qualification = qualificationMapper.selectById(alert.getQualificationId());
        Supplier supplier = supplierMapper.selectById(alert.getSupplierId());
        
        String supplierName = supplier != null ? supplier.getSupplierName() : null;
        String qualificationName = qualification != null ? qualification.getQualificationName() : null;
        
        return QualificationAlertVO.builder()
            .id(alert.getId())
            .qualificationId(alert.getQualificationId())
            .supplierId(alert.getSupplierId())
            .supplierName(supplierName)
            .qualificationName(qualificationName)
            .alertType(alert.getAlertType())
            .alertTitle(alert.getAlertTitle())
            .alertContent(alert.getAlertContent())
            .alertDate(alert.getAlertDate())
            .daysBeforeExpiry(alert.getDaysBeforeExpiry())
            .isRead(alert.getIsRead())
            .readTime(alert.getReadTime())
            .createTime(alert.getCreateTime())
            .build();
    }
}
