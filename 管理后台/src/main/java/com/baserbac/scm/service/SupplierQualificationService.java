package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.common.util.SecurityUtil;
import com.baserbac.scm.dto.QualificationAuditDTO;
import com.baserbac.scm.dto.QualificationCreateDTO;
import com.baserbac.scm.dto.QualificationQueryDTO;
import com.baserbac.scm.dto.QualificationUpdateDTO;
import com.baserbac.scm.entity.QualificationAlert;
import com.baserbac.scm.entity.Supplier;
import com.baserbac.scm.entity.SupplierQualification;
import com.baserbac.scm.mapper.QualificationAlertMapper;
import com.baserbac.scm.mapper.SupplierMapper;
import com.baserbac.scm.mapper.SupplierQualificationMapper;
import com.baserbac.scm.vo.SupplierQualificationVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierQualificationService {

    private final SupplierQualificationMapper qualificationMapper;
    private final SupplierMapper supplierMapper;
    private final QualificationAlertMapper alertMapper;

    private static final int ALERT_DAYS_BEFORE = 30;

    public PageResult<SupplierQualificationVO> pageQualifications(QualificationQueryDTO queryDTO) {
        log.debug("pageQualifications called with: supplierName={}, supplierId={}, pageNum={}, pageSize={}",
                queryDTO.getSupplierName(), queryDTO.getSupplierId(), queryDTO.getPageNum(), queryDTO.getPageSize());
        
        Page<SupplierQualification> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        List<Long> supplierIds = null;
        if (queryDTO.getSupplierName() != null && !queryDTO.getSupplierName().trim().isEmpty()) {
            LambdaQueryWrapper<Supplier> supplierWrapper = new LambdaQueryWrapper<>();
            supplierWrapper.like(Supplier::getSupplierName, queryDTO.getSupplierName().trim());
            List<Supplier> suppliers = supplierMapper.selectList(supplierWrapper);
            if (suppliers != null && !suppliers.isEmpty()) {
                supplierIds = suppliers.stream().map(Supplier::getId).collect(Collectors.toList());
                log.debug("Found {} suppliers by name: {}", supplierIds.size(), supplierIds);
            } else {
                log.debug("No suppliers found by name: {}", queryDTO.getSupplierName());
                return PageResult.of(0L, new ArrayList<>(), (long) queryDTO.getPageNum(), (long) queryDTO.getPageSize());
            }
        }
        
        LambdaQueryWrapper<SupplierQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getSupplierId() != null, SupplierQualification::getSupplierId, queryDTO.getSupplierId())
               .in(supplierIds != null && !supplierIds.isEmpty(), SupplierQualification::getSupplierId, supplierIds)
               .eq(queryDTO.getQualificationType() != null, SupplierQualification::getQualificationType, queryDTO.getQualificationType())
               .like(queryDTO.getQualificationName() != null, SupplierQualification::getQualificationName, queryDTO.getQualificationName())
               .eq(queryDTO.getAuditStatus() != null, SupplierQualification::getAuditStatus, queryDTO.getAuditStatus())
               .eq(queryDTO.getAlertStatus() != null, SupplierQualification::getAlertStatus, queryDTO.getAlertStatus())
               .orderByDesc(SupplierQualification::getCreateTime);
        
        log.debug("Query wrapper: {}", wrapper.getSqlSegment());
        
        Page<SupplierQualification> result = qualificationMapper.selectPage(page, wrapper);
        
        log.debug("Query result: total={}, records={}", result.getTotal(), result.getRecords().size());
        
        List<SupplierQualificationVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    public SupplierQualificationVO getQualificationById(Long id) {
        SupplierQualification qualification = qualificationMapper.selectById(id);
        if (qualification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(qualification);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createQualification(QualificationCreateDTO createDTO) {
        Supplier supplier = supplierMapper.selectById(createDTO.getSupplierId());
        if (supplier == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        SupplierQualification qualification = new SupplierQualification();
        qualification.setSupplierId(createDTO.getSupplierId());
        qualification.setQualificationType(createDTO.getQualificationType());
        qualification.setQualificationName(createDTO.getQualificationName());
        qualification.setCertificateNo(createDTO.getCertificateNo());
        qualification.setIssueDate(createDTO.getIssueDate());
        qualification.setExpiryDate(createDTO.getExpiryDate());
        qualification.setIsLongTerm(createDTO.getIsLongTerm());
        qualification.setIssuingAuthority(createDTO.getIssuingAuthority());
        qualification.setRemark(createDTO.getRemark());
        
        if (createDTO.getFileUrls() != null && !createDTO.getFileUrls().isEmpty()) {
            qualification.setFileUrls(String.join(",", createDTO.getFileUrls()));
        }
        
        qualification.setAuditStatus(0);
        qualification.setAlertStatus(0);
        qualification.setAlertSent(0);
        
        updateAlertStatus(qualification);
        
        qualificationMapper.insert(qualification);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateQualification(QualificationUpdateDTO updateDTO) {
        SupplierQualification qualification = qualificationMapper.selectById(updateDTO.getId());
        if (qualification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (updateDTO.getQualificationName() != null) {
            qualification.setQualificationName(updateDTO.getQualificationName());
        }
        if (updateDTO.getCertificateNo() != null) {
            qualification.setCertificateNo(updateDTO.getCertificateNo());
        }
        if (updateDTO.getIssueDate() != null) {
            qualification.setIssueDate(updateDTO.getIssueDate());
        }
        if (updateDTO.getExpiryDate() != null) {
            qualification.setExpiryDate(updateDTO.getExpiryDate());
        }
        if (updateDTO.getIsLongTerm() != null) {
            qualification.setIsLongTerm(updateDTO.getIsLongTerm());
        }
        if (updateDTO.getFileUrls() != null) {
            qualification.setFileUrls(String.join(",", updateDTO.getFileUrls()));
        }
        if (updateDTO.getIssuingAuthority() != null) {
            qualification.setIssuingAuthority(updateDTO.getIssuingAuthority());
        }
        if (updateDTO.getRemark() != null) {
            qualification.setRemark(updateDTO.getRemark());
        }
        
        qualification.setAuditStatus(0);
        qualification.setAlertSent(0);
        
        updateAlertStatus(qualification);
        
        qualificationMapper.updateById(qualification);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteQualification(Long id) {
        SupplierQualification qualification = qualificationMapper.selectById(id);
        if (qualification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        qualificationMapper.deleteById(id);
        
        alertMapper.delete(
            new LambdaQueryWrapper<QualificationAlert>()
                .eq(QualificationAlert::getQualificationId, id)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditQualification(Long id, QualificationAuditDTO auditDTO) {
        SupplierQualification qualification = qualificationMapper.selectById(id);
        if (qualification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        qualification.setAuditStatus(auditDTO.getAuditStatus());
        qualification.setAuditRemark(auditDTO.getAuditRemark());
        qualification.setAuditBy(SecurityUtil.getCurrentUsername());
        qualification.setAuditTime(LocalDateTime.now());
        
        qualificationMapper.updateById(qualification);
    }

    private void updateAlertStatus(SupplierQualification qualification) {
        if (qualification.getIsLongTerm() != null && qualification.getIsLongTerm() == 1) {
            qualification.setAlertStatus(0);
            return;
        }
        
        LocalDate expiryDate = qualification.getExpiryDate();
        if (expiryDate == null) {
            qualification.setAlertStatus(0);
            return;
        }
        
        LocalDate now = LocalDate.now();
        long daysToExpiry = ChronoUnit.DAYS.between(now, expiryDate);
        
        if (daysToExpiry < 0) {
            qualification.setAlertStatus(2);
        } else if (daysToExpiry <= ALERT_DAYS_BEFORE) {
            qualification.setAlertStatus(1);
        } else {
            qualification.setAlertStatus(0);
        }
    }

    private SupplierQualificationVO convertToVO(SupplierQualification q) {
        Supplier supplier = supplierMapper.selectById(q.getSupplierId());
        String supplierName = supplier != null ? supplier.getSupplierName() : null;
        
        List<String> fileUrls = null;
        if (q.getFileUrls() != null && !q.getFileUrls().isEmpty()) {
            fileUrls = Arrays.asList(q.getFileUrls().split(","));
        }
        
        Integer daysToExpiry = null;
        if (q.getExpiryDate() != null && (q.getIsLongTerm() == null || q.getIsLongTerm() != 1)) {
            daysToExpiry = (int) ChronoUnit.DAYS.between(LocalDate.now(), q.getExpiryDate());
        }
        
        return SupplierQualificationVO.builder()
            .id(q.getId())
            .supplierId(q.getSupplierId())
            .supplierName(supplierName)
            .qualificationType(q.getQualificationType())
            .qualificationName(q.getQualificationName())
            .certificateNo(q.getCertificateNo())
            .issueDate(q.getIssueDate())
            .expiryDate(q.getExpiryDate())
            .isLongTerm(q.getIsLongTerm())
            .fileUrls(fileUrls)
            .issuingAuthority(q.getIssuingAuthority())
            .auditStatus(q.getAuditStatus())
            .auditRemark(q.getAuditRemark())
            .auditBy(q.getAuditBy())
            .auditTime(q.getAuditTime())
            .alertStatus(q.getAlertStatus())
            .daysToExpiry(daysToExpiry)
            .remark(q.getRemark())
            .createTime(q.getCreateTime())
            .updateTime(q.getUpdateTime())
            .build();
    }

    public void addTestData() {
        // 查找现有供应商
        List<Supplier> suppliers = supplierMapper.selectList(null);
        if (suppliers.isEmpty()) {
            return;
        }
        
        // 为每个供应商添加资质
        for (Supplier supplier : suppliers) {
            // 营业执照
            QualificationCreateDTO dto1 = new QualificationCreateDTO();
            dto1.setSupplierId(supplier.getId());
            dto1.setQualificationType("BUSINESS_LICENSE");
            dto1.setQualificationName("营业执照");
            dto1.setCertificateNo("91310000X" + supplier.getId());
            dto1.setIssuingAuthority("工商行政管理局");
            dto1.setIssueDate(LocalDate.now().minusYears(1));
            dto1.setExpiryDate(LocalDate.now().plusYears(1));
            dto1.setIsLongTerm(0);
            dto1.setRemark("供应商营业执照");
            
            try {
                createQualification(dto1);
            } catch (Exception e) {
                // 忽略已存在的记录
            }
            
            // 税务登记证
            QualificationCreateDTO dto2 = new QualificationCreateDTO();
            dto2.setSupplierId(supplier.getId());
            dto2.setQualificationType("TAX_REGISTRATION");
            dto2.setQualificationName("税务登记证");
            dto2.setCertificateNo("31000000X" + supplier.getId());
            dto2.setIssuingAuthority("税务局");
            dto2.setIssueDate(LocalDate.now().minusYears(1));
            dto2.setExpiryDate(LocalDate.now().plusYears(1));
            dto2.setIsLongTerm(0);
            dto2.setRemark("供应商税务登记证");
            
            try {
                createQualification(dto2);
            } catch (Exception e) {
                // 忽略已存在的记录
            }
        }
    }
}
