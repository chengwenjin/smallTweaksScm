package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.PurchaseRequirementCreateDTO;
import com.baserbac.scm.dto.PurchaseRequirementQueryDTO;
import com.baserbac.scm.dto.PurchaseRequirementUpdateDTO;
import com.baserbac.scm.entity.PurchaseRequirement;
import com.baserbac.scm.mapper.PurchaseRequirementMapper;
import com.baserbac.scm.vo.PurchaseRequirementVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseRequirementService {

    private final PurchaseRequirementMapper requirementMapper;

    private static final String REQ_NO_PREFIX = "REQ";

    public PageResult<PurchaseRequirementVO> pageRequirements(PurchaseRequirementQueryDTO queryDTO) {
        Page<PurchaseRequirement> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<PurchaseRequirement> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getReqNo() != null, PurchaseRequirement::getReqNo, queryDTO.getReqNo())
               .like(queryDTO.getReqName() != null, PurchaseRequirement::getReqName, queryDTO.getReqName())
               .like(queryDTO.getMaterialName() != null, PurchaseRequirement::getMaterialName, queryDTO.getMaterialName())
               .eq(queryDTO.getStatus() != null, PurchaseRequirement::getStatus, queryDTO.getStatus())
               .eq(queryDTO.getUrgency() != null, PurchaseRequirement::getUrgency, queryDTO.getUrgency())
               .like(queryDTO.getReqDept() != null, PurchaseRequirement::getReqDept, queryDTO.getReqDept())
               .orderByDesc(PurchaseRequirement::getCreateTime);
        
        Page<PurchaseRequirement> result = requirementMapper.selectPage(page, wrapper);
        
        List<PurchaseRequirementVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    public PurchaseRequirementVO getRequirementById(Long id) {
        PurchaseRequirement requirement = requirementMapper.selectById(id);
        if (requirement == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(requirement);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createRequirement(PurchaseRequirementCreateDTO createDTO) {
        PurchaseRequirement requirement = new PurchaseRequirement();
        requirement.setReqNo(generateReqNo());
        requirement.setReqName(createDTO.getReqName());
        requirement.setMaterialName(createDTO.getMaterialName());
        requirement.setMaterialSpec(createDTO.getMaterialSpec());
        requirement.setMaterialUnit(createDTO.getMaterialUnit());
        requirement.setQuantity(createDTO.getQuantity());
        requirement.setRequiredDate(createDTO.getRequiredDate());
        requirement.setUrgency(createDTO.getUrgency() != null ? createDTO.getUrgency() : 1);
        requirement.setBudgetRange(createDTO.getBudgetRange());
        requirement.setDescription(createDTO.getDescription());
        requirement.setStatus(0);
        requirement.setReqDept(createDTO.getReqDept());
        requirement.setReqPerson(createDTO.getReqPerson());
        requirement.setReqPhone(createDTO.getReqPhone());
        requirement.setDeliveryDate(createDTO.getDeliveryDate());
        requirement.setDeliveryAddress(createDTO.getDeliveryAddress());
        requirement.setRemark(createDTO.getRemark());
        requirement.setIsDeleted(0);
        
        requirementMapper.insert(requirement);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRequirement(PurchaseRequirementUpdateDTO updateDTO) {
        PurchaseRequirement requirement = requirementMapper.selectById(updateDTO.getId());
        if (requirement == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (updateDTO.getReqName() != null) {
            requirement.setReqName(updateDTO.getReqName());
        }
        if (updateDTO.getMaterialName() != null) {
            requirement.setMaterialName(updateDTO.getMaterialName());
        }
        if (updateDTO.getMaterialSpec() != null) {
            requirement.setMaterialSpec(updateDTO.getMaterialSpec());
        }
        if (updateDTO.getMaterialUnit() != null) {
            requirement.setMaterialUnit(updateDTO.getMaterialUnit());
        }
        if (updateDTO.getQuantity() != null) {
            requirement.setQuantity(updateDTO.getQuantity());
        }
        if (updateDTO.getRequiredDate() != null) {
            requirement.setRequiredDate(updateDTO.getRequiredDate());
        }
        if (updateDTO.getUrgency() != null) {
            requirement.setUrgency(updateDTO.getUrgency());
        }
        if (updateDTO.getBudgetRange() != null) {
            requirement.setBudgetRange(updateDTO.getBudgetRange());
        }
        if (updateDTO.getDescription() != null) {
            requirement.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getStatus() != null) {
            requirement.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getReqDept() != null) {
            requirement.setReqDept(updateDTO.getReqDept());
        }
        if (updateDTO.getReqPerson() != null) {
            requirement.setReqPerson(updateDTO.getReqPerson());
        }
        if (updateDTO.getReqPhone() != null) {
            requirement.setReqPhone(updateDTO.getReqPhone());
        }
        if (updateDTO.getDeliveryDate() != null) {
            requirement.setDeliveryDate(updateDTO.getDeliveryDate());
        }
        if (updateDTO.getDeliveryAddress() != null) {
            requirement.setDeliveryAddress(updateDTO.getDeliveryAddress());
        }
        if (updateDTO.getRemark() != null) {
            requirement.setRemark(updateDTO.getRemark());
        }
        
        requirementMapper.updateById(requirement);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRequirement(Long id) {
        PurchaseRequirement requirement = requirementMapper.selectById(id);
        if (requirement == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        requirementMapper.deleteById(id);
    }

    private String generateReqNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return REQ_NO_PREFIX + dateStr + System.currentTimeMillis();
    }

    private PurchaseRequirementVO convertToVO(PurchaseRequirement req) {
        return PurchaseRequirementVO.builder()
            .id(req.getId())
            .reqNo(req.getReqNo())
            .reqName(req.getReqName())
            .materialName(req.getMaterialName())
            .materialSpec(req.getMaterialSpec())
            .materialUnit(req.getMaterialUnit())
            .quantity(req.getQuantity())
            .requiredDate(req.getRequiredDate())
            .urgency(req.getUrgency())
            .budgetRange(req.getBudgetRange())
            .description(req.getDescription())
            .status(req.getStatus())
            .reqDept(req.getReqDept())
            .reqPerson(req.getReqPerson())
            .reqPhone(req.getReqPhone())
            .deliveryDate(req.getDeliveryDate())
            .deliveryAddress(req.getDeliveryAddress())
            .remark(req.getRemark())
            .createTime(req.getCreateTime())
            .updateTime(req.getUpdateTime())
            .build();
    }
}
