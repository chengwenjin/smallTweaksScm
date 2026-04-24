package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.BlacklistCreateDTO;
import com.baserbac.scm.dto.BlacklistQueryDTO;
import com.baserbac.scm.dto.BlacklistRemoveDTO;
import com.baserbac.scm.entity.Supplier;
import com.baserbac.scm.entity.SupplierBlacklist;
import com.baserbac.scm.mapper.SupplierBlacklistMapper;
import com.baserbac.scm.mapper.SupplierMapper;
import com.baserbac.scm.vo.SupplierBlacklistVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierBlacklistService {

    private final SupplierBlacklistMapper blacklistMapper;
    private final SupplierMapper supplierMapper;

    private static final String[] BLACKLIST_TYPE_NAMES = {"", "严重违约", "质量问题", "欺诈行为", "其他"};
    private static final String[] STATUS_NAMES = {"", "在黑名单", "已移除"};

    public PageResult<SupplierBlacklistVO> pageBlacklists(BlacklistQueryDTO queryDTO) {
        Page<SupplierBlacklist> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<SupplierBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getSupplierCode() != null && !queryDTO.getSupplierCode().isEmpty(), 
                SupplierBlacklist::getSupplierCode, queryDTO.getSupplierCode())
               .like(queryDTO.getSupplierName() != null && !queryDTO.getSupplierName().isEmpty(), 
                SupplierBlacklist::getSupplierName, queryDTO.getSupplierName())
               .eq(queryDTO.getBlacklistType() != null, 
                SupplierBlacklist::getBlacklistType, queryDTO.getBlacklistType())
               .eq(queryDTO.getStatus() != null, 
                SupplierBlacklist::getStatus, queryDTO.getStatus())
               .orderByDesc(SupplierBlacklist::getCreateTime);
        
        Page<SupplierBlacklist> result = blacklistMapper.selectPage(page, wrapper);
        
        List<SupplierBlacklistVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    public SupplierBlacklistVO getBlacklistById(Long id) {
        SupplierBlacklist blacklist = blacklistMapper.selectById(id);
        if (blacklist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(blacklist);
    }

    public boolean isSupplierBlacklisted(Long supplierId) {
        if (supplierId == null) {
            return false;
        }
        Long count = blacklistMapper.selectCount(
            new LambdaQueryWrapper<SupplierBlacklist>()
                .eq(SupplierBlacklist::getSupplierId, supplierId)
                .eq(SupplierBlacklist::getStatus, 1)
        );
        return count != null && count > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addToBlacklist(BlacklistCreateDTO createDTO) {
        Supplier supplier = supplierMapper.selectById(createDTO.getSupplierId());
        if (supplier == null) {
            throw new BusinessException(4004, "供应商不存在");
        }

        if (isSupplierBlacklisted(createDTO.getSupplierId())) {
            throw new BusinessException(4005, "该供应商已在黑名单中");
        }

        if (createDTO.getIsPermanent() == 0 && createDTO.getExpireDate() == null) {
            throw new BusinessException(4006, "非永久黑名单请填写到期日期");
        }

        SupplierBlacklist blacklist = new SupplierBlacklist();
        blacklist.setSupplierId(supplier.getId());
        blacklist.setSupplierCode(supplier.getSupplierCode());
        blacklist.setSupplierName(supplier.getSupplierName());
        blacklist.setBlacklistType(createDTO.getBlacklistType());
        blacklist.setBlacklistReason(createDTO.getBlacklistReason());
        blacklist.setBlacklistDate(LocalDate.now());
        blacklist.setIsPermanent(createDTO.getIsPermanent());
        blacklist.setExpireDate(createDTO.getExpireDate());
        blacklist.setStatus(1);
        blacklist.setRemark(createDTO.getRemark());
        
        blacklistMapper.insert(blacklist);
        
        supplier.setStatus(3);
        supplierMapper.updateById(supplier);
        
        log.info("供应商已加入黑名单: supplierId={}, supplierName={}", supplier.getId(), supplier.getSupplierName());
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFromBlacklist(Long id, BlacklistRemoveDTO removeDTO) {
        SupplierBlacklist blacklist = blacklistMapper.selectById(id);
        if (blacklist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        if (blacklist.getStatus() == 2) {
            throw new BusinessException(4007, "该记录已从黑名单移除");
        }

        blacklist.setStatus(2);
        blacklist.setRemoveReason(removeDTO.getRemoveReason());
        blacklist.setRemoveDate(LocalDate.now());
        
        blacklistMapper.updateById(blacklist);
        
        Supplier supplier = supplierMapper.selectById(blacklist.getSupplierId());
        if (supplier != null) {
            supplier.setStatus(1);
            supplierMapper.updateById(supplier);
        }
        
        log.info("供应商已从黑名单移除: supplierId={}, supplierName={}", blacklist.getSupplierId(), blacklist.getSupplierName());
    }

    public void checkExpiredBlacklists() {
        LocalDate today = LocalDate.now();
        
        List<SupplierBlacklist> expiredList = blacklistMapper.selectList(
            new LambdaQueryWrapper<SupplierBlacklist>()
                .eq(SupplierBlacklist::getStatus, 1)
                .eq(SupplierBlacklist::getIsPermanent, 0)
                .lt(SupplierBlacklist::getExpireDate, today)
        );
        
        for (SupplierBlacklist blacklist : expiredList) {
            blacklist.setStatus(2);
            blacklist.setRemoveReason("黑名单到期自动移除");
            blacklist.setRemoveDate(today);
            blacklistMapper.updateById(blacklist);
            
            Supplier supplier = supplierMapper.selectById(blacklist.getSupplierId());
            if (supplier != null) {
                supplier.setStatus(1);
                supplierMapper.updateById(supplier);
            }
            
            log.info("黑名单到期自动移除: supplierId={}", blacklist.getSupplierId());
        }
    }

    private SupplierBlacklistVO convertToVO(SupplierBlacklist blacklist) {
        return SupplierBlacklistVO.builder()
            .id(blacklist.getId())
            .supplierId(blacklist.getSupplierId())
            .supplierCode(blacklist.getSupplierCode())
            .supplierName(blacklist.getSupplierName())
            .blacklistType(blacklist.getBlacklistType())
            .blacklistTypeName(getBlacklistTypeName(blacklist.getBlacklistType()))
            .blacklistReason(blacklist.getBlacklistReason())
            .blacklistDate(blacklist.getBlacklistDate())
            .isPermanent(blacklist.getIsPermanent())
            .expireDate(blacklist.getExpireDate())
            .status(blacklist.getStatus())
            .statusName(getStatusName(blacklist.getStatus()))
            .removeReason(blacklist.getRemoveReason())
            .removeDate(blacklist.getRemoveDate())
            .remark(blacklist.getRemark())
            .createTime(blacklist.getCreateTime())
            .updateTime(blacklist.getUpdateTime())
            .build();
    }

    private String getBlacklistTypeName(Integer type) {
        if (type == null || type < 1 || type >= BLACKLIST_TYPE_NAMES.length) {
            return "未知";
        }
        return BLACKLIST_TYPE_NAMES[type];
    }

    private String getStatusName(Integer status) {
        if (status == null || status < 1 || status >= STATUS_NAMES.length) {
            return "未知";
        }
        return STATUS_NAMES[status];
    }
}
