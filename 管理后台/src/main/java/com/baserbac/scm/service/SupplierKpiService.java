package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.SupplierKpiQueryDTO;
import com.baserbac.scm.entity.Supplier;
import com.baserbac.scm.entity.SupplierKpi;
import com.baserbac.scm.mapper.SupplierKpiMapper;
import com.baserbac.scm.mapper.SupplierMapper;
import com.baserbac.scm.vo.SupplierKpiVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierKpiService {

    private final SupplierKpiMapper kpiMapper;
    private final SupplierMapper supplierMapper;

    private static final String[] PERIOD_TYPE_NAMES = {"", "月度", "季度", "年度"};
    private static final String[] GRADE_NAMES = {"", "A级", "AA级", "AAA级"};

    public PageResult<SupplierKpiVO> pageKpis(SupplierKpiQueryDTO queryDTO) {
        Page<SupplierKpi> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<SupplierKpi> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getSupplierName() != null, SupplierKpi::getSupplierName, queryDTO.getSupplierName())
               .eq(queryDTO.getPeriodType() != null, SupplierKpi::getPeriodType, queryDTO.getPeriodType())
               .eq(queryDTO.getYear() != null, SupplierKpi::getYear, queryDTO.getYear())
               .eq(queryDTO.getQuarter() != null, SupplierKpi::getQuarter, queryDTO.getQuarter())
               .eq(queryDTO.getGrade() != null, SupplierKpi::getGrade, queryDTO.getGrade())
               .orderByDesc(SupplierKpi::getYear)
               .orderByDesc(SupplierKpi::getQuarter)
               .orderByDesc(SupplierKpi::getCreateTime);
        
        Page<SupplierKpi> result = kpiMapper.selectPage(page, wrapper);
        
        List<SupplierKpiVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    public SupplierKpiVO getKpiById(Long id) {
        SupplierKpi kpi = kpiMapper.selectById(id);
        if (kpi == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(kpi);
    }

    @Transactional(rollbackFor = Exception.class)
    public void calculateKpis(Integer periodType, Integer year, Integer quarter, Integer month) {
        List<Supplier> suppliers = supplierMapper.selectList(
            new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getStatus, 1)
                .eq(Supplier::getIsDeleted, 0)
        );
        
        for (Supplier supplier : suppliers) {
            calculateSupplierKpi(supplier, periodType, year, quarter, month);
        }
        
        log.info("KPI计算完成，共处理{}个供应商", suppliers.size());
    }

    private void calculateSupplierKpi(Supplier supplier, Integer periodType, Integer year, Integer quarter, Integer month) {
        LambdaQueryWrapper<SupplierKpi> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(SupplierKpi::getSupplierId, supplier.getId())
                   .eq(SupplierKpi::getPeriodType, periodType)
                   .eq(SupplierKpi::getYear, year);
        
        if (periodType == 1) {
            existWrapper.eq(SupplierKpi::getMonth, month);
        } else if (periodType == 2) {
            existWrapper.eq(SupplierKpi::getQuarter, quarter);
        }
        
        SupplierKpi existKpi = kpiMapper.selectOne(existWrapper);
        
        SupplierKpi kpi = existKpi != null ? existKpi : new SupplierKpi();
        kpi.setSupplierId(supplier.getId());
        kpi.setSupplierName(supplier.getSupplierName());
        kpi.setPeriodType(periodType);
        kpi.setYear(year);
        kpi.setQuarter(quarter);
        kpi.setMonth(month);
        
        BigDecimal deliveryRate = simulateDeliveryRate(supplier);
        kpi.setDeliveryOnTimeRate(deliveryRate);
        kpi.setDeliveryTotalCount(100);
        kpi.setDeliveryOnTimeCount(deliveryRate.multiply(new BigDecimal("100")).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP).intValue());
        
        BigDecimal qualityRate = simulateQualityRate(supplier);
        kpi.setQualityPassRate(qualityRate);
        kpi.setQualityTotalCount(80);
        kpi.setQualityPassCount(qualityRate.multiply(new BigDecimal("100")).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP).intValue());
        
        BigDecimal priceScore = simulatePriceScore(supplier);
        kpi.setPriceCompetitiveness(priceScore);
        kpi.setPriceCompareCount(20);
        kpi.setPriceBestCount(priceScore.multiply(new BigDecimal("20")).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP).intValue());
        
        BigDecimal serviceScore = simulateServiceScore(supplier);
        kpi.setServiceResponseSpeed(serviceScore);
        kpi.setServiceTotalCount(30);
        kpi.setServiceResponseOnTimeCount(serviceScore.multiply(new BigDecimal("30")).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP).intValue());
        
        BigDecimal totalScore = calculateTotalScore(deliveryRate, qualityRate, priceScore, serviceScore);
        kpi.setTotalScore(totalScore);
        kpi.setGrade(calculateGrade(totalScore));
        kpi.setStatus(1);
        
        if (existKpi == null) {
            kpiMapper.insert(kpi);
        } else {
            kpiMapper.updateById(kpi);
        }
    }

    private BigDecimal simulateDeliveryRate(Supplier supplier) {
        int baseRate = 85;
        if (supplier.getGrade() != null) {
            baseRate += (supplier.getGrade() - 1) * 5;
        }
        if (supplier.getCooperationLevel() != null && supplier.getCooperationLevel() == 1) {
            baseRate += 5;
        }
        baseRate += (int) (Math.random() * 10) - 5;
        baseRate = Math.min(100, Math.max(60, baseRate));
        return new BigDecimal(baseRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal simulateQualityRate(Supplier supplier) {
        int baseRate = 90;
        if (supplier.getGrade() != null) {
            baseRate += (supplier.getGrade() - 1) * 3;
        }
        baseRate += (int) (Math.random() * 8) - 4;
        baseRate = Math.min(100, Math.max(70, baseRate));
        return new BigDecimal(baseRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal simulatePriceScore(Supplier supplier) {
        int baseRate = 75;
        if (supplier.getSupplierType() != null && supplier.getSupplierType() == 1) {
            baseRate += 10;
        }
        baseRate += (int) (Math.random() * 15) - 7;
        baseRate = Math.min(100, Math.max(50, baseRate));
        return new BigDecimal(baseRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal simulateServiceScore(Supplier supplier) {
        int baseRate = 80;
        if (supplier.getCooperationLevel() != null) {
            baseRate += (supplier.getCooperationLevel() - 1) * 5;
        }
        baseRate += (int) (Math.random() * 12) - 6;
        baseRate = Math.min(100, Math.max(55, baseRate));
        return new BigDecimal(baseRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalScore(BigDecimal delivery, BigDecimal quality, BigDecimal price, BigDecimal service) {
        return delivery.multiply(new BigDecimal("0.30"))
            .add(quality.multiply(new BigDecimal("0.30")))
            .add(price.multiply(new BigDecimal("0.20")))
            .add(service.multiply(new BigDecimal("0.20")))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private Integer calculateGrade(BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal("90")) >= 0) {
            return 3;
        } else if (totalScore.compareTo(new BigDecimal("80")) >= 0) {
            return 2;
        } else {
            return 1;
        }
    }

    private SupplierKpiVO convertToVO(SupplierKpi kpi) {
        return SupplierKpiVO.builder()
            .id(kpi.getId())
            .supplierId(kpi.getSupplierId())
            .supplierName(kpi.getSupplierName())
            .periodType(kpi.getPeriodType())
            .periodTypeName(kpi.getPeriodType() != null && kpi.getPeriodType() > 0 && kpi.getPeriodType() <= 3 
                ? PERIOD_TYPE_NAMES[kpi.getPeriodType()] : "")
            .year(kpi.getYear())
            .quarter(kpi.getQuarter())
            .month(kpi.getMonth())
            .deliveryOnTimeRate(kpi.getDeliveryOnTimeRate())
            .deliveryTotalCount(kpi.getDeliveryTotalCount())
            .deliveryOnTimeCount(kpi.getDeliveryOnTimeCount())
            .qualityPassRate(kpi.getQualityPassRate())
            .qualityTotalCount(kpi.getQualityTotalCount())
            .qualityPassCount(kpi.getQualityPassCount())
            .priceCompetitiveness(kpi.getPriceCompetitiveness())
            .priceCompareCount(kpi.getPriceCompareCount())
            .priceBestCount(kpi.getPriceBestCount())
            .serviceResponseSpeed(kpi.getServiceResponseSpeed())
            .serviceTotalCount(kpi.getServiceTotalCount())
            .serviceResponseOnTimeCount(kpi.getServiceResponseOnTimeCount())
            .totalScore(kpi.getTotalScore())
            .grade(kpi.getGrade())
            .gradeName(kpi.getGrade() != null && kpi.getGrade() > 0 && kpi.getGrade() <= 3 
                ? GRADE_NAMES[kpi.getGrade()] : "")
            .status(kpi.getStatus())
            .remark(kpi.getRemark())
            .createTime(kpi.getCreateTime())
            .updateTime(kpi.getUpdateTime())
            .build();
    }
}
