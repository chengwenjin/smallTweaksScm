package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.SupplierCreateDTO;
import com.baserbac.scm.dto.SupplierQueryDTO;
import com.baserbac.scm.dto.SupplierUpdateDTO;
import com.baserbac.scm.entity.Supplier;
import com.baserbac.scm.entity.SupplierQualification;
import com.baserbac.scm.mapper.SupplierMapper;
import com.baserbac.scm.mapper.SupplierQualificationMapper;
import com.baserbac.scm.vo.SupplierQualificationVO;
import com.baserbac.scm.vo.SupplierVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierMapper supplierMapper;
    private final SupplierQualificationMapper qualificationMapper;

    public PageResult<SupplierVO> pageSuppliers(SupplierQueryDTO queryDTO) {
        Page<Supplier> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getSupplierCode() != null, Supplier::getSupplierCode, queryDTO.getSupplierCode())
               .like(queryDTO.getSupplierName() != null, Supplier::getSupplierName, queryDTO.getSupplierName())
               .eq(queryDTO.getSupplierType() != null, Supplier::getSupplierType, queryDTO.getSupplierType())
               .eq(queryDTO.getGrade() != null, Supplier::getGrade, queryDTO.getGrade())
               .eq(queryDTO.getMaterialCategory() != null, Supplier::getMaterialCategory, queryDTO.getMaterialCategory())
               .eq(queryDTO.getCooperationLevel() != null, Supplier::getCooperationLevel, queryDTO.getCooperationLevel())
               .eq(queryDTO.getStatus() != null, Supplier::getStatus, queryDTO.getStatus())
               .eq(queryDTO.getAuditStatus() != null, Supplier::getAuditStatus, queryDTO.getAuditStatus())
               .orderByDesc(Supplier::getCreateTime);
        
        Page<Supplier> result = supplierMapper.selectPage(page, wrapper);
        
        List<SupplierVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    public SupplierVO getSupplierById(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVOWithQualifications(supplier);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createSupplier(SupplierCreateDTO createDTO) {
        Long count = supplierMapper.selectCount(
            new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getSupplierCode, createDTO.getSupplierCode())
        );
        if (count > 0) {
            throw new BusinessException(4002, "供应商编码已存在");
        }

        Supplier supplier = new Supplier();
        supplier.setSupplierCode(createDTO.getSupplierCode());
        supplier.setSupplierName(createDTO.getSupplierName());
        supplier.setSupplierType(createDTO.getSupplierType());
        supplier.setGrade(createDTO.getGrade());
        supplier.setMaterialCategory(createDTO.getMaterialCategory());
        supplier.setCooperationLevel(createDTO.getCooperationLevel());
        supplier.setContactPerson(createDTO.getContactPerson());
        supplier.setContactPhone(createDTO.getContactPhone());
        supplier.setContactEmail(createDTO.getContactEmail());
        supplier.setAddress(createDTO.getAddress());
        supplier.setStatus(createDTO.getStatus());
        supplier.setAuditStatus(createDTO.getAuditStatus());
        supplier.setRemark(createDTO.getRemark());
        
        supplierMapper.insert(supplier);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSupplier(SupplierUpdateDTO updateDTO) {
        Supplier supplier = supplierMapper.selectById(updateDTO.getId());
        if (supplier == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (updateDTO.getSupplierName() != null) {
            supplier.setSupplierName(updateDTO.getSupplierName());
        }
        if (updateDTO.getSupplierType() != null) {
            supplier.setSupplierType(updateDTO.getSupplierType());
        }
        if (updateDTO.getGrade() != null) {
            supplier.setGrade(updateDTO.getGrade());
        }
        if (updateDTO.getMaterialCategory() != null) {
            supplier.setMaterialCategory(updateDTO.getMaterialCategory());
        }
        if (updateDTO.getCooperationLevel() != null) {
            supplier.setCooperationLevel(updateDTO.getCooperationLevel());
        }
        if (updateDTO.getContactPerson() != null) {
            supplier.setContactPerson(updateDTO.getContactPerson());
        }
        if (updateDTO.getContactPhone() != null) {
            supplier.setContactPhone(updateDTO.getContactPhone());
        }
        if (updateDTO.getContactEmail() != null) {
            supplier.setContactEmail(updateDTO.getContactEmail());
        }
        if (updateDTO.getAddress() != null) {
            supplier.setAddress(updateDTO.getAddress());
        }
        if (updateDTO.getStatus() != null) {
            supplier.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getAuditStatus() != null) {
            supplier.setAuditStatus(updateDTO.getAuditStatus());
        }
        if (updateDTO.getAuditRemark() != null) {
            supplier.setAuditRemark(updateDTO.getAuditRemark());
        }
        if (updateDTO.getRemark() != null) {
            supplier.setRemark(updateDTO.getRemark());
        }
        
        supplierMapper.updateById(supplier);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        supplierMapper.deleteById(id);
        
        qualificationMapper.delete(
            new LambdaQueryWrapper<SupplierQualification>()
                .eq(SupplierQualification::getSupplierId, id)
        );
    }

    private SupplierVO convertToVO(Supplier supplier) {
        return SupplierVO.builder()
            .id(supplier.getId())
            .supplierCode(supplier.getSupplierCode())
            .supplierName(supplier.getSupplierName())
            .supplierType(supplier.getSupplierType())
            .grade(supplier.getGrade())
            .materialCategory(supplier.getMaterialCategory())
            .cooperationLevel(supplier.getCooperationLevel())
            .contactPerson(supplier.getContactPerson())
            .contactPhone(supplier.getContactPhone())
            .contactEmail(supplier.getContactEmail())
            .address(supplier.getAddress())
            .status(supplier.getStatus())
            .auditStatus(supplier.getAuditStatus())
            .auditRemark(supplier.getAuditRemark())
            .remark(supplier.getRemark())
            .createTime(supplier.getCreateTime())
            .updateTime(supplier.getUpdateTime())
            .build();
    }

    private SupplierVO convertToVOWithQualifications(Supplier supplier) {
        List<SupplierQualification> qualifications = qualificationMapper.selectList(
            new LambdaQueryWrapper<SupplierQualification>()
                .eq(SupplierQualification::getSupplierId, supplier.getId())
                .orderByDesc(SupplierQualification::getCreateTime)
        );
        
        List<SupplierQualificationVO> qualificationVOList = qualifications.stream()
            .map(this::convertQualificationToVO)
            .collect(Collectors.toList());
        
        return SupplierVO.builder()
            .id(supplier.getId())
            .supplierCode(supplier.getSupplierCode())
            .supplierName(supplier.getSupplierName())
            .supplierType(supplier.getSupplierType())
            .grade(supplier.getGrade())
            .materialCategory(supplier.getMaterialCategory())
            .cooperationLevel(supplier.getCooperationLevel())
            .contactPerson(supplier.getContactPerson())
            .contactPhone(supplier.getContactPhone())
            .contactEmail(supplier.getContactEmail())
            .address(supplier.getAddress())
            .status(supplier.getStatus())
            .auditStatus(supplier.getAuditStatus())
            .auditRemark(supplier.getAuditRemark())
            .remark(supplier.getRemark())
            .createTime(supplier.getCreateTime())
            .updateTime(supplier.getUpdateTime())
            .qualifications(qualificationVOList)
            .build();
    }

    private SupplierQualificationVO convertQualificationToVO(SupplierQualification q) {
        List<String> fileUrls = null;
        if (q.getFileUrls() != null && !q.getFileUrls().isEmpty()) {
            fileUrls = Arrays.asList(q.getFileUrls().split(","));
        }
        
        Integer daysToExpiry = null;
        if (q.getExpiryDate() != null && q.getIsLongTerm() != 1) {
            daysToExpiry = (int) ChronoUnit.DAYS.between(LocalDate.now(), q.getExpiryDate());
        }
        
        return SupplierQualificationVO.builder()
            .id(q.getId())
            .supplierId(q.getSupplierId())
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
}
