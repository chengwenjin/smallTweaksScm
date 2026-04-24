package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.InquiryCreateDTO;
import com.baserbac.scm.dto.InquiryQueryDTO;
import com.baserbac.scm.dto.QuoteSubmitDTO;
import com.baserbac.scm.entity.Inquiry;
import com.baserbac.scm.entity.InquirySupplier;
import com.baserbac.scm.entity.PurchaseRequirement;
import com.baserbac.scm.entity.Supplier;
import com.baserbac.scm.mapper.InquiryMapper;
import com.baserbac.scm.mapper.InquirySupplierMapper;
import com.baserbac.scm.mapper.PurchaseRequirementMapper;
import com.baserbac.scm.mapper.SupplierMapper;
import com.baserbac.scm.vo.InquirySupplierVO;
import com.baserbac.scm.vo.InquiryVO;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryMapper inquiryMapper;
    private final InquirySupplierMapper inquirySupplierMapper;
    private final PurchaseRequirementMapper requirementMapper;
    private final SupplierMapper supplierMapper;

    private static final String INQUIRY_NO_PREFIX = "INQ";

    public PageResult<InquiryVO> pageInquiries(InquiryQueryDTO queryDTO) {
        Page<Inquiry> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Inquiry> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getInquiryNo() != null, Inquiry::getInquiryNo, queryDTO.getInquiryNo())
               .like(queryDTO.getInquiryName() != null, Inquiry::getInquiryName, queryDTO.getInquiryName())
               .eq(queryDTO.getStatus() != null, Inquiry::getStatus, queryDTO.getStatus())
               .orderByDesc(Inquiry::getCreateTime);
        
        Page<Inquiry> result = inquiryMapper.selectPage(page, wrapper);
        
        List<InquiryVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    public InquiryVO getInquiryById(Long id) {
        Inquiry inquiry = inquiryMapper.selectById(id);
        if (inquiry == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVOWithDetails(inquiry);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createInquiry(InquiryCreateDTO createDTO) {
        Inquiry inquiry = new Inquiry();
        inquiry.setInquiryNo(generateInquiryNo());
        inquiry.setInquiryName(createDTO.getInquiryName());
        inquiry.setStatus(0);
        inquiry.setDeadline(createDTO.getDeadline());
        inquiry.setContactPerson(createDTO.getContactPerson());
        inquiry.setContactPhone(createDTO.getContactPhone());
        inquiry.setContactEmail(createDTO.getContactEmail());
        inquiry.setDescription(createDTO.getDescription());
        inquiry.setRemark(createDTO.getRemark());
        inquiry.setIsDeleted(0);
        
        if (createDTO.getReqIds() != null && !createDTO.getReqIds().isEmpty()) {
            inquiry.setReqIds(String.join(",", createDTO.getReqIds().stream().map(String::valueOf).collect(Collectors.toList())));
        }
        
        inquiryMapper.insert(inquiry);
        
        if (createDTO.getSupplierIds() != null && !createDTO.getSupplierIds().isEmpty()) {
            for (Long supplierId : createDTO.getSupplierIds()) {
                Supplier supplier = supplierMapper.selectById(supplierId);
                if (supplier != null) {
                    InquirySupplier inquirySupplier = new InquirySupplier();
                    inquirySupplier.setInquiryId(inquiry.getId());
                    inquirySupplier.setSupplierId(supplierId);
                    inquirySupplier.setSupplierName(supplier.getSupplierName());
                    inquirySupplier.setInviteStatus(1);
                    inquirySupplier.setQuoteStatus(0);
                    inquirySupplier.setIsDeleted(0);
                    inquirySupplierMapper.insert(inquirySupplier);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitQuote(QuoteSubmitDTO submitDTO) {
        InquirySupplier inquirySupplier = inquirySupplierMapper.selectOne(
            new LambdaQueryWrapper<InquirySupplier>()
                .eq(InquirySupplier::getInquiryId, submitDTO.getInquiryId())
                .eq(InquirySupplier::getSupplierId, submitDTO.getSupplierId())
        );
        
        if (inquirySupplier == null) {
            throw new BusinessException(4001, "未找到该供应商的询价邀请");
        }
        
        inquirySupplier.setQuoteStatus(1);
        inquirySupplier.setUnitPrice(submitDTO.getUnitPrice());
        inquirySupplier.setTotalPrice(submitDTO.getTotalPrice());
        inquirySupplier.setDeliveryDate(submitDTO.getDeliveryDate());
        inquirySupplier.setPaymentTerms(submitDTO.getPaymentTerms());
        inquirySupplier.setWarranty(submitDTO.getWarranty());
        inquirySupplier.setQuoteRemark(submitDTO.getQuoteRemark());
        inquirySupplier.setQuoteTime(LocalDateTime.now());
        
        inquirySupplierMapper.updateById(inquirySupplier);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishInquiry(Long id) {
        Inquiry inquiry = inquiryMapper.selectById(id);
        if (inquiry == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        inquiry.setStatus(1);
        inquiryMapper.updateById(inquiry);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelInquiry(Long id) {
        Inquiry inquiry = inquiryMapper.selectById(id);
        if (inquiry == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        inquiry.setStatus(5);
        inquiryMapper.updateById(inquiry);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteInquiry(Long id) {
        Inquiry inquiry = inquiryMapper.selectById(id);
        if (inquiry == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        inquirySupplierMapper.delete(
            new LambdaQueryWrapper<InquirySupplier>()
                .eq(InquirySupplier::getInquiryId, id)
        );
        
        inquiryMapper.deleteById(id);
    }

    private String generateInquiryNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return INQUIRY_NO_PREFIX + dateStr + System.currentTimeMillis();
    }

    private InquiryVO convertToVO(Inquiry inquiry) {
        return InquiryVO.builder()
            .id(inquiry.getId())
            .inquiryNo(inquiry.getInquiryNo())
            .inquiryName(inquiry.getInquiryName())
            .reqIds(inquiry.getReqIds())
            .status(inquiry.getStatus())
            .deadline(inquiry.getDeadline())
            .contactPerson(inquiry.getContactPerson())
            .contactPhone(inquiry.getContactPhone())
            .contactEmail(inquiry.getContactEmail())
            .description(inquiry.getDescription())
            .remark(inquiry.getRemark())
            .createTime(inquiry.getCreateTime())
            .updateTime(inquiry.getUpdateTime())
            .build();
    }

    private InquiryVO convertToVOWithDetails(Inquiry inquiry) {
        InquiryVO vo = convertToVO(inquiry);
        
        List<InquirySupplier> suppliers = inquirySupplierMapper.selectList(
            new LambdaQueryWrapper<InquirySupplier>()
                .eq(InquirySupplier::getInquiryId, inquiry.getId())
        );
        
        List<InquirySupplierVO> supplierVOList = suppliers.stream()
            .map(this::convertSupplierToVO)
            .collect(Collectors.toList());
        vo.setSuppliers(supplierVOList);
        
        if (inquiry.getReqIds() != null && !inquiry.getReqIds().isEmpty()) {
            List<Long> reqIdList = Arrays.stream(inquiry.getReqIds().split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
            
            List<PurchaseRequirement> requirements = requirementMapper.selectBatchIds(reqIdList);
            List<PurchaseRequirementVO> reqVOList = requirements.stream()
                .map(this::convertRequirementToVO)
                .collect(Collectors.toList());
            vo.setRequirements(reqVOList);
        }
        
        return vo;
    }

    private InquirySupplierVO convertSupplierToVO(InquirySupplier s) {
        return InquirySupplierVO.builder()
            .id(s.getId())
            .inquiryId(s.getInquiryId())
            .supplierId(s.getSupplierId())
            .supplierName(s.getSupplierName())
            .inviteStatus(s.getInviteStatus())
            .quoteStatus(s.getQuoteStatus())
            .unitPrice(s.getUnitPrice())
            .totalPrice(s.getTotalPrice())
            .deliveryDate(s.getDeliveryDate())
            .paymentTerms(s.getPaymentTerms())
            .warranty(s.getWarranty())
            .quoteRemark(s.getQuoteRemark())
            .quoteTime(s.getQuoteTime())
            .remark(s.getRemark())
            .createTime(s.getCreateTime())
            .updateTime(s.getUpdateTime())
            .build();
    }

    private PurchaseRequirementVO convertRequirementToVO(PurchaseRequirement req) {
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
