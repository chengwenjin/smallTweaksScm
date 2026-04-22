package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.SupplierClassificationDTO;
import com.baserbac.scm.entity.Supplier;
import com.baserbac.scm.entity.SupplierClassificationLog;
import com.baserbac.scm.mapper.SupplierClassificationLogMapper;
import com.baserbac.scm.mapper.SupplierMapper;
import com.baserbac.scm.vo.SupplierClassificationLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierClassificationService {

    private final SupplierMapper supplierMapper;
    private final SupplierClassificationLogMapper classificationLogMapper;

    @Transactional(rollbackFor = Exception.class)
    public void setClassification(SupplierClassificationDTO dto) {
        List<Long> supplierIds = dto.getSupplierIds();
        for (Long supplierId : supplierIds) {
            Supplier supplier = supplierMapper.selectById(supplierId);
            if (supplier == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "供应商不存在：" + supplierId);
            }

            SupplierClassificationLog log = new SupplierClassificationLog();
            log.setSupplierId(supplierId);
            log.setOldMaterialCategory(supplier.getMaterialCategory());
            log.setNewMaterialCategory(dto.getMaterialCategory());
            log.setOldCooperationLevel(supplier.getCooperationLevel());
            log.setNewCooperationLevel(dto.getCooperationLevel());
            log.setChangeReason(dto.getChangeReason());

            if (dto.getMaterialCategory() != null) {
                supplier.setMaterialCategory(dto.getMaterialCategory());
            }
            if (dto.getCooperationLevel() != null) {
                supplier.setCooperationLevel(dto.getCooperationLevel());
            }

            supplierMapper.updateById(supplier);
            classificationLogMapper.insert(log);
        }
    }

    public PageResult<SupplierClassificationLogVO> pageClassificationLogs(int pageNum, int pageSize, Long supplierId) {
        Page<SupplierClassificationLog> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SupplierClassificationLog> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(SupplierClassificationLog::getSupplierId, supplierId);
        }
        wrapper.orderByDesc(SupplierClassificationLog::getCreateTime);

        Page<SupplierClassificationLog> result = classificationLogMapper.selectPage(page, wrapper);

        List<SupplierClassificationLogVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return PageResult.of(
            result.getTotal(),
            voList,
            (long) result.getCurrent(),
            (long) result.getSize()
        );
    }

    private SupplierClassificationLogVO convertToVO(SupplierClassificationLog log) {
        Supplier supplier = supplierMapper.selectById(log.getSupplierId());
        String supplierCode = supplier != null ? supplier.getSupplierCode() : "";
        String supplierName = supplier != null ? supplier.getSupplierName() : "";

        return SupplierClassificationLogVO.builder()
            .id(log.getId())
            .supplierId(log.getSupplierId())
            .supplierCode(supplierCode)
            .supplierName(supplierName)
            .oldMaterialCategory(log.getOldMaterialCategory())
            .newMaterialCategory(log.getNewMaterialCategory())
            .oldCooperationLevel(log.getOldCooperationLevel())
            .newCooperationLevel(log.getNewCooperationLevel())
            .changeReason(log.getChangeReason())
            .createBy(log.getCreateBy())
            .createTime(log.getCreateTime())
            .build();
    }
}
