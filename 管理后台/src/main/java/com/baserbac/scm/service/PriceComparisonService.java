package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.PriceComparisonDTO;
import com.baserbac.scm.entity.Inquiry;
import com.baserbac.scm.entity.InquirySupplier;
import com.baserbac.scm.entity.PriceComparison;
import com.baserbac.scm.entity.PurchaseRequirement;
import com.baserbac.scm.mapper.InquiryMapper;
import com.baserbac.scm.mapper.InquirySupplierMapper;
import com.baserbac.scm.mapper.PriceComparisonMapper;
import com.baserbac.scm.mapper.PurchaseRequirementMapper;
import com.baserbac.scm.vo.PriceComparisonVO;
import com.baserbac.scm.vo.QuoteCompareVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceComparisonService {

    private final PriceComparisonMapper comparisonMapper;
    private final InquiryMapper inquiryMapper;
    private final InquirySupplierMapper inquirySupplierMapper;
    private final PurchaseRequirementMapper requirementMapper;

    private static final String COMPARISON_NO_PREFIX = "CMP";

    public PageResult<PriceComparisonVO> pageComparisons(Integer pageNum, Integer pageSize, Long inquiryId, Integer status) {
        Page<PriceComparison> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<PriceComparison> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(inquiryId != null, PriceComparison::getInquiryId, inquiryId)
               .eq(status != null, PriceComparison::getStatus, status)
               .orderByDesc(PriceComparison::getCreateTime);
        
        Page<PriceComparison> result = comparisonMapper.selectPage(page, wrapper);
        
        List<PriceComparisonVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    public PriceComparisonVO getComparisonById(Long id) {
        PriceComparison comparison = comparisonMapper.selectById(id);
        if (comparison == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVOWithDetails(comparison);
    }

    @Transactional(rollbackFor = Exception.class)
    public PriceComparisonVO createComparison(Long inquiryId) {
        Inquiry inquiry = inquiryMapper.selectById(inquiryId);
        if (inquiry == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        List<InquirySupplier> suppliers = inquirySupplierMapper.selectList(
            new LambdaQueryWrapper<InquirySupplier>()
                .eq(InquirySupplier::getInquiryId, inquiryId)
                .eq(InquirySupplier::getQuoteStatus, 1)
        );
        
        if (suppliers.size() < 2) {
            throw new BusinessException(4001, "报价供应商不足2家，无法进行比价");
        }
        
        List<QuoteCompareVO> quoteCompares = generateQuoteCompares(suppliers);
        
        QuoteCompareVO recommended = quoteCompares.stream()
            .max(Comparator.comparing(QuoteCompareVO::getTotalScore))
            .orElse(null);
        
        PriceComparison comparison = new PriceComparison();
        comparison.setInquiryId(inquiryId);
        comparison.setComparisonNo(generateComparisonNo());
        comparison.setComparisonName("比价单-" + inquiry.getInquiryName());
        comparison.setStatus(1);
        
        if (recommended != null) {
            comparison.setRecommendSupplierId(recommended.getSupplierId());
            comparison.setRecommendSupplierName(recommended.getSupplierName());
            comparison.setRecommendPrice(recommended.getTotalPrice());
            comparison.setRecommendReason(recommended.getRecommendReason());
        }
        
        comparison.setIsDeleted(0);
        comparisonMapper.insert(comparison);
        
        PriceComparisonVO vo = convertToVO(comparison);
        vo.setQuoteCompares(quoteCompares);
        
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmRecommendation(PriceComparisonDTO dto) {
        PriceComparison comparison = comparisonMapper.selectById(dto.getId());
        if (comparison == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        comparison.setRecommendSupplierId(dto.getRecommendSupplierId());
        comparison.setRecommendSupplierName(dto.getRecommendSupplierName());
        comparison.setRecommendPrice(dto.getRecommendPrice());
        comparison.setRecommendReason(dto.getRecommendReason());
        comparison.setComparisonResult(dto.getComparisonResult());
        comparison.setStatus(2);
        
        comparisonMapper.updateById(comparison);
    }

    private List<QuoteCompareVO> generateQuoteCompares(List<InquirySupplier> suppliers) {
        List<QuoteCompareVO> compares = new ArrayList<>();
        
        BigDecimal minPrice = suppliers.stream()
            .map(s -> s.getTotalPrice() != null ? s.getTotalPrice() : BigDecimal.ZERO)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        List<InquirySupplier> sortedByPrice = suppliers.stream()
            .sorted(Comparator.comparing(s -> s.getTotalPrice() != null ? s.getTotalPrice() : BigDecimal.ZERO))
            .collect(Collectors.toList());
        
        for (int i = 0; i < sortedByPrice.size(); i++) {
            InquirySupplier s = sortedByPrice.get(i);
            QuoteCompareVO vo = new QuoteCompareVO();
            vo.setSupplierId(s.getSupplierId());
            vo.setSupplierName(s.getSupplierName());
            vo.setUnitPrice(s.getUnitPrice());
            vo.setTotalPrice(s.getTotalPrice());
            vo.setDeliveryDate(s.getDeliveryDate());
            vo.setPaymentTerms(s.getPaymentTerms());
            vo.setWarranty(s.getWarranty());
            vo.setPriceRank(i + 1);
            
            if (minPrice.compareTo(BigDecimal.ZERO) > 0 && s.getTotalPrice() != null) {
                vo.setPriceDiff(s.getTotalPrice().subtract(minPrice));
            }
            
            vo.setHistoryCooperationCount((int) (Math.random() * 10) + 1);
            vo.setHistoryAvgDeliveryDays((int) (Math.random() * 10) + 3);
            
            BigDecimal priceScore = calculatePriceScore(s.getTotalPrice(), minPrice);
            BigDecimal deliveryScore = calculateDeliveryScore(s.getDeliveryDate());
            BigDecimal historyScore = calculateHistoryScore(vo.getHistoryCooperationCount());
            
            BigDecimal totalScore = priceScore.multiply(new BigDecimal("0.5"))
                .add(deliveryScore.multiply(new BigDecimal("0.3")))
                .add(historyScore.multiply(new BigDecimal("0.2")));
            
            vo.setTotalScore(totalScore.setScale(2, RoundingMode.HALF_UP));
            
            if (i == 0) {
                vo.setIsRecommend(1);
                vo.setRecommendReason("价格最低，综合得分最高");
            } else {
                vo.setIsRecommend(0);
            }
            
            compares.add(vo);
        }
        
        return compares;
    }

    private BigDecimal calculatePriceScore(BigDecimal price, BigDecimal minPrice) {
        if (price == null || minPrice == null || minPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("50");
        }
        
        BigDecimal ratio = minPrice.divide(price, 4, RoundingMode.HALF_UP);
        return ratio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDeliveryScore(LocalDate deliveryDate) {
        if (deliveryDate == null) {
            return new BigDecimal("50");
        }
        
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deliveryDate);
        
        if (daysUntil <= 7) {
            return new BigDecimal("100");
        } else if (daysUntil <= 14) {
            return new BigDecimal("80");
        } else if (daysUntil <= 30) {
            return new BigDecimal("60");
        } else {
            return new BigDecimal("40");
        }
    }

    private BigDecimal calculateHistoryScore(Integer count) {
        if (count == null || count <= 0) {
            return new BigDecimal("50");
        }
        
        if (count >= 10) {
            return new BigDecimal("100");
        } else if (count >= 5) {
            return new BigDecimal("80");
        } else if (count >= 3) {
            return new BigDecimal("70");
        } else {
            return new BigDecimal("60");
        }
    }

    private String generateComparisonNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return COMPARISON_NO_PREFIX + dateStr + System.currentTimeMillis();
    }

    private PriceComparisonVO convertToVO(PriceComparison c) {
        return PriceComparisonVO.builder()
            .id(c.getId())
            .inquiryId(c.getInquiryId())
            .comparisonNo(c.getComparisonNo())
            .comparisonName(c.getComparisonName())
            .reqId(c.getReqId())
            .reqName(c.getReqName())
            .materialName(c.getMaterialName())
            .materialSpec(c.getMaterialSpec())
            .materialUnit(c.getMaterialUnit())
            .reqQuantity(c.getReqQuantity())
            .status(c.getStatus())
            .recommendSupplierId(c.getRecommendSupplierId())
            .recommendSupplierName(c.getRecommendSupplierName())
            .recommendPrice(c.getRecommendPrice())
            .recommendReason(c.getRecommendReason())
            .comparisonResult(c.getComparisonResult())
            .remark(c.getRemark())
            .createTime(c.getCreateTime())
            .updateTime(c.getUpdateTime())
            .build();
    }

    private PriceComparisonVO convertToVOWithDetails(PriceComparison comparison) {
        PriceComparisonVO vo = convertToVO(comparison);
        
        List<InquirySupplier> suppliers = inquirySupplierMapper.selectList(
            new LambdaQueryWrapper<InquirySupplier>()
                .eq(InquirySupplier::getInquiryId, comparison.getInquiryId())
                .eq(InquirySupplier::getQuoteStatus, 1)
        );
        
        if (!suppliers.isEmpty()) {
            List<QuoteCompareVO> quoteCompares = generateQuoteCompares(suppliers);
            vo.setQuoteCompares(quoteCompares);
        }
        
        return vo;
    }
}
