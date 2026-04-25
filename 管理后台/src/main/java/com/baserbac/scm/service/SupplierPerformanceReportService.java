package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.PerformanceReportQueryDTO;
import com.baserbac.scm.entity.Supplier;
import com.baserbac.scm.entity.SupplierKpi;
import com.baserbac.scm.entity.SupplierPerformanceReport;
import com.baserbac.scm.mapper.SupplierKpiMapper;
import com.baserbac.scm.mapper.SupplierMapper;
import com.baserbac.scm.mapper.SupplierPerformanceReportMapper;
import com.baserbac.scm.vo.PerformanceReportVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierPerformanceReportService {

    private final SupplierPerformanceReportMapper reportMapper;
    private final SupplierKpiMapper kpiMapper;
    private final SupplierMapper supplierMapper;

    private static final String[] REPORT_TYPE_NAMES = {"", "季度报告", "年度报告"};
    private static final String[] GRADE_NAMES = {"", "A级", "AA级", "AAA级"};

    public PageResult<PerformanceReportVO> pageReports(PerformanceReportQueryDTO queryDTO) {
        Page<SupplierPerformanceReport> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<SupplierPerformanceReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getReportNo() != null, SupplierPerformanceReport::getReportNo, queryDTO.getReportNo())
               .like(queryDTO.getReportName() != null, SupplierPerformanceReport::getReportName, queryDTO.getReportName())
               .eq(queryDTO.getReportType() != null, SupplierPerformanceReport::getReportType, queryDTO.getReportType())
               .eq(queryDTO.getYear() != null, SupplierPerformanceReport::getYear, queryDTO.getYear())
               .eq(queryDTO.getQuarter() != null, SupplierPerformanceReport::getQuarter, queryDTO.getQuarter())
               .like(queryDTO.getSupplierName() != null, SupplierPerformanceReport::getSupplierName, queryDTO.getSupplierName())
               .eq(queryDTO.getGrade() != null, SupplierPerformanceReport::getGrade, queryDTO.getGrade())
               .orderByDesc(SupplierPerformanceReport::getYear)
               .orderByDesc(SupplierPerformanceReport::getQuarter)
               .orderByDesc(SupplierPerformanceReport::getRanking);
        
        Page<SupplierPerformanceReport> result = reportMapper.selectPage(page, wrapper);
        
        List<PerformanceReportVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    public PerformanceReportVO getReportById(Long id) {
        SupplierPerformanceReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(report);
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateReports(Integer reportType, Integer year, Integer quarter) {
        int periodType = reportType == 1 ? 2 : 3;
        
        LambdaQueryWrapper<SupplierKpi> kpiWrapper = new LambdaQueryWrapper<>();
        kpiWrapper.eq(SupplierKpi::getPeriodType, periodType)
                 .eq(SupplierKpi::getYear, year);
        
        if (reportType == 1) {
            kpiWrapper.eq(SupplierKpi::getQuarter, quarter);
        }
        
        List<SupplierKpi> kpiList = kpiMapper.selectList(kpiWrapper);
        
        if (kpiList.isEmpty()) {
            log.warn("没有找到指定周期的KPI数据，报告类型:{}, 年份:{}, 季度:{}", reportType, year, quarter);
            return;
        }
        
        Map<Long, List<SupplierKpi>> supplierKpiMap = kpiList.stream()
            .collect(Collectors.groupingBy(SupplierKpi::getSupplierId));
        
        int totalSuppliers = supplierKpiMap.size();
        
        List<SupplierKpiAvg> avgKpiList = new ArrayList<>();
        
        for (Map.Entry<Long, List<SupplierKpi>> entry : supplierKpiMap.entrySet()) {
            Long supplierId = entry.getKey();
            List<SupplierKpi> kpis = entry.getValue();
            
            BigDecimal avgDelivery = kpis.stream()
                .map(kpi -> kpi.getDeliveryOnTimeRate() != null ? kpi.getDeliveryOnTimeRate() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(kpis.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal avgQuality = kpis.stream()
                .map(kpi -> kpi.getQualityPassRate() != null ? kpi.getQualityPassRate() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(kpis.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal avgPrice = kpis.stream()
                .map(kpi -> kpi.getPriceCompetitiveness() != null ? kpi.getPriceCompetitiveness() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(kpis.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal avgService = kpis.stream()
                .map(kpi -> kpi.getServiceResponseSpeed() != null ? kpi.getServiceResponseSpeed() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(kpis.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal avgTotal = avgDelivery.multiply(new BigDecimal("0.30"))
                .add(avgQuality.multiply(new BigDecimal("0.30")))
                .add(avgPrice.multiply(new BigDecimal("0.20")))
                .add(avgService.multiply(new BigDecimal("0.20")))
                .setScale(2, RoundingMode.HALF_UP);
            
            avgKpiList.add(new SupplierKpiAvg(
                supplierId,
                kpis.get(0).getSupplierName(),
                avgDelivery,
                avgQuality,
                avgPrice,
                avgService,
                avgTotal
            ));
        }
        
        avgKpiList.sort(Comparator.comparing(SupplierKpiAvg::getAvgTotal).reversed());
        
        for (int i = 0; i < avgKpiList.size(); i++) {
            SupplierKpiAvg avg = avgKpiList.get(i);
            int ranking = i + 1;
            createOrUpdateReport(avg, ranking, totalSuppliers, reportType, year, quarter);
        }
        
        log.info("绩效报告生成完成，共{}份报告", avgKpiList.size());
    }

    private void createOrUpdateReport(SupplierKpiAvg avg, int ranking, int totalSuppliers, 
                                       Integer reportType, Integer year, Integer quarter) {
        LambdaQueryWrapper<SupplierPerformanceReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierPerformanceReport::getSupplierId, avg.getSupplierId())
               .eq(SupplierPerformanceReport::getReportType, reportType)
               .eq(SupplierPerformanceReport::getYear, year);
        
        if (reportType == 1) {
            wrapper.eq(SupplierPerformanceReport::getQuarter, quarter);
        }
        
        SupplierPerformanceReport existReport = reportMapper.selectOne(wrapper);
        
        SupplierPerformanceReport report = existReport != null ? existReport : new SupplierPerformanceReport();
        
        String reportNo = generateReportNo(reportType, year, quarter);
        String reportName = generateReportName(avg.getSupplierName(), reportType, year, quarter);
        
        if (existReport == null) {
            report.setReportNo(reportNo);
        }
        report.setReportName(reportName);
        report.setReportType(reportType);
        report.setYear(year);
        report.setQuarter(quarter);
        report.setSupplierId(avg.getSupplierId());
        report.setSupplierName(avg.getSupplierName());
        report.setDeliveryOnTimeRate(avg.getAvgDelivery());
        report.setQualityPassRate(avg.getAvgQuality());
        report.setPriceCompetitiveness(avg.getAvgPrice());
        report.setServiceResponseSpeed(avg.getAvgService());
        report.setTotalScore(avg.getAvgTotal());
        report.setGrade(calculateGrade(avg.getAvgTotal()));
        report.setRanking(ranking);
        report.setTotalSuppliers(totalSuppliers);
        
        BigDecimal previousDelivery = calculatePreviousScore(avg.getSupplierId(), "delivery", reportType, year, quarter);
        BigDecimal previousQuality = calculatePreviousScore(avg.getSupplierId(), "quality", reportType, year, quarter);
        BigDecimal previousPrice = calculatePreviousScore(avg.getSupplierId(), "price", reportType, year, quarter);
        BigDecimal previousService = calculatePreviousScore(avg.getSupplierId(), "service", reportType, year, quarter);
        
        report.setPreviousDeliveryRate(previousDelivery);
        report.setPreviousQualityRate(previousQuality);
        report.setPreviousPriceScore(previousPrice);
        report.setPreviousServiceScore(previousService);
        
        if (previousDelivery != null && previousQuality != null && previousPrice != null && previousService != null) {
            BigDecimal previousTotal = previousDelivery.multiply(new BigDecimal("0.30"))
                .add(previousQuality.multiply(new BigDecimal("0.30")))
                .add(previousPrice.multiply(new BigDecimal("0.20")))
                .add(previousService.multiply(new BigDecimal("0.20")))
                .setScale(2, RoundingMode.HALF_UP);
            report.setPreviousTotalScore(previousTotal);
            report.setPreviousGrade(calculateGrade(previousTotal));
        }
        
        report.setQuotaSuggestion(calculateQuotaSuggestion(ranking, totalSuppliers));
        report.setStrengthAnalysis(generateStrengthAnalysis(avg));
        report.setWeaknessAnalysis(generateWeaknessAnalysis(avg));
        report.setImprovementSuggestion(generateImprovementSuggestion(avg));
        
        report.setStartDate(calculateStartDate(reportType, year, quarter));
        report.setEndDate(calculateEndDate(reportType, year, quarter));
        report.setStatus(1);
        
        if (existReport == null) {
            reportMapper.insert(report);
        } else {
            reportMapper.updateById(report);
        }
    }

    private String generateReportNo(Integer reportType, Integer year, Integer quarter) {
        String prefix = reportType == 1 ? "RPT-Q" : "RPT-Y";
        String dateStr = year.toString();
        if (reportType == 1) {
            dateStr += quarter;
        }
        return prefix + dateStr + LocalDate.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));
    }

    private String generateReportName(String supplierName, Integer reportType, Integer year, Integer quarter) {
        if (reportType == 1) {
            return supplierName + " - " + year + "年第" + quarter + "季度绩效报告";
        } else {
            return supplierName + " - " + year + "年度绩效报告";
        }
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

    private BigDecimal calculatePreviousScore(Long supplierId, String type, Integer reportType, Integer year, Integer quarter) {
        int previousYear = year - 1;
        int previousQuarter = reportType == 1 ? quarter : 0;
        int periodType = reportType == 1 ? 2 : 3;
        
        LambdaQueryWrapper<SupplierKpi> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierKpi::getSupplierId, supplierId)
               .eq(SupplierKpi::getPeriodType, periodType)
               .eq(SupplierKpi::getYear, previousYear);
        
        if (periodType == 2) {
            wrapper.eq(SupplierKpi::getQuarter, previousQuarter);
        }
        
        List<SupplierKpi> kpis = kpiMapper.selectList(wrapper);
        
        if (kpis.isEmpty()) {
            return null;
        }
        
        BigDecimal total = BigDecimal.ZERO;
        for (SupplierKpi kpi : kpis) {
            BigDecimal value = switch (type) {
                case "delivery" -> kpi.getDeliveryOnTimeRate();
                case "quality" -> kpi.getQualityPassRate();
                case "price" -> kpi.getPriceCompetitiveness();
                case "service" -> kpi.getServiceResponseSpeed();
                default -> BigDecimal.ZERO;
            };
            if (value != null) {
                total = total.add(value);
            }
        }
        
        return total.divide(new BigDecimal(kpis.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateQuotaSuggestion(int ranking, int totalSuppliers) {
        double ratio;
        if (ranking <= Math.ceil(totalSuppliers * 0.2)) {
            ratio = 1.2;
        } else if (ranking <= Math.ceil(totalSuppliers * 0.5)) {
            ratio = 1.0;
        } else if (ranking <= Math.ceil(totalSuppliers * 0.8)) {
            ratio = 0.8;
        } else {
            ratio = 0.5;
        }
        return new BigDecimal(ratio).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateStrengthAnalysis(SupplierKpiAvg avg) {
        List<String> strengths = new ArrayList<>();
        if (avg.getAvgQuality().compareTo(new BigDecimal("90")) >= 0) {
            strengths.add("来料质检合格率高，产品质量稳定可靠");
        }
        if (avg.getAvgDelivery().compareTo(new BigDecimal("90")) >= 0) {
            strengths.add("交付准时率高，供应链响应速度快");
        }
        if (avg.getAvgPrice().compareTo(new BigDecimal("80")) >= 0) {
            strengths.add("价格竞争力较强，性价比高");
        }
        if (strengths.isEmpty()) {
            return "综合表现中等，各指标相对均衡";
        }
        return String.join("；", strengths);
    }

    private String generateWeaknessAnalysis(SupplierKpiAvg avg) {
        List<String> weaknesses = new ArrayList<>();
        if (avg.getAvgDelivery().compareTo(new BigDecimal("80")) < 0) {
            weaknesses.add("交付准时率偏低，需要关注供应链稳定性");
        }
        if (avg.getAvgQuality().compareTo(new BigDecimal("80")) < 0) {
            weaknesses.add("来料质检合格率偏低，产品质量稳定性待提升");
        }
        if (avg.getAvgService().compareTo(new BigDecimal("75")) < 0) {
            weaknesses.add("售后服务响应速度较慢，客户体验待改善");
        }
        if (weaknesses.isEmpty()) {
            return "整体表现良好，暂无明显短板";
        }
        return String.join("；", weaknesses);
    }

    private String generateImprovementSuggestion(SupplierKpiAvg avg) {
        List<String> suggestions = new ArrayList<>();
        
        if (avg.getAvgDelivery().compareTo(new BigDecimal("85")) < 0) {
            suggestions.add("建议加强与供应商的沟通，建立交付预警机制，提升交付准时率");
        }
        if (avg.getAvgQuality().compareTo(new BigDecimal("85")) < 0) {
            suggestions.add("建议加强来料检验，与供应商一起分析质量问题根源，持续改进产品质量");
        }
        if (avg.getAvgService().compareTo(new BigDecimal("80")) < 0) {
            suggestions.add("建议建立快速响应机制，优化售后服务流程，提升客户满意度");
        }
        
        if (suggestions.isEmpty()) {
            return "继续保持良好表现，寻求持续改进机会，巩固竞争优势";
        }
        return String.join("。", suggestions);
    }

    private LocalDate calculateStartDate(Integer reportType, Integer year, Integer quarter) {
        if (reportType == 1) {
            int startMonth = (quarter - 1) * 3 + 1;
            return LocalDate.of(year, startMonth, 1);
        } else {
            return LocalDate.of(year, 1, 1);
        }
    }

    private LocalDate calculateEndDate(Integer reportType, Integer year, Integer quarter) {
        if (reportType == 1) {
            int endMonth = quarter * 3;
            return LocalDate.of(year, endMonth, LocalDate.of(year, endMonth, 1).lengthOfMonth());
        } else {
            return LocalDate.of(year, 12, 31);
        }
    }

    private PerformanceReportVO convertToVO(SupplierPerformanceReport report) {
        return PerformanceReportVO.builder()
            .id(report.getId())
            .reportNo(report.getReportNo())
            .reportName(report.getReportName())
            .reportType(report.getReportType())
            .reportTypeName(report.getReportType() != null && report.getReportType() > 0 && report.getReportType() <= 2 
                ? REPORT_TYPE_NAMES[report.getReportType()] : "")
            .year(report.getYear())
            .quarter(report.getQuarter())
            .supplierId(report.getSupplierId())
            .supplierName(report.getSupplierName())
            .deliveryOnTimeRate(report.getDeliveryOnTimeRate())
            .qualityPassRate(report.getQualityPassRate())
            .priceCompetitiveness(report.getPriceCompetitiveness())
            .serviceResponseSpeed(report.getServiceResponseSpeed())
            .totalScore(report.getTotalScore())
            .grade(report.getGrade())
            .gradeName(report.getGrade() != null && report.getGrade() > 0 && report.getGrade() <= 3 
                ? GRADE_NAMES[report.getGrade()] : "")
            .ranking(report.getRanking())
            .totalSuppliers(report.getTotalSuppliers())
            .previousDeliveryRate(report.getPreviousDeliveryRate())
            .previousQualityRate(report.getPreviousQualityRate())
            .previousPriceScore(report.getPreviousPriceScore())
            .previousServiceScore(report.getPreviousServiceScore())
            .previousTotalScore(report.getPreviousTotalScore())
            .previousGrade(report.getPreviousGrade())
            .previousRanking(report.getPreviousRanking())
            .quotaSuggestion(report.getQuotaSuggestion())
            .strengthAnalysis(report.getStrengthAnalysis())
            .weaknessAnalysis(report.getWeaknessAnalysis())
            .improvementSuggestion(report.getImprovementSuggestion())
            .startDate(report.getStartDate())
            .endDate(report.getEndDate())
            .status(report.getStatus())
            .approvedBy(report.getApprovedBy())
            .approveTime(report.getApproveTime())
            .remark(report.getRemark())
            .createTime(report.getCreateTime())
            .updateTime(report.getUpdateTime())
            .build();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class SupplierKpiAvg {
        private Long supplierId;
        private String supplierName;
        private BigDecimal avgDelivery;
        private BigDecimal avgQuality;
        private BigDecimal avgPrice;
        private BigDecimal avgService;
        private BigDecimal avgTotal;
    }
}
