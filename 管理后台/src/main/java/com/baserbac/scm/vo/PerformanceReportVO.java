package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "绩效报告信息")
public class PerformanceReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "报告ID")
    private Long id;

    @Schema(description = "报告编号")
    private String reportNo;

    @Schema(description = "报告名称")
    private String reportName;

    @Schema(description = "报告类型：1季度 2年度")
    private Integer reportType;

    @Schema(description = "报告类型名称")
    private String reportTypeName;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "季度")
    private Integer quarter;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "交付准时率")
    private BigDecimal deliveryOnTimeRate;

    @Schema(description = "来料质检合格率")
    private BigDecimal qualityPassRate;

    @Schema(description = "价格竞争力")
    private BigDecimal priceCompetitiveness;

    @Schema(description = "售后服务响应速度")
    private BigDecimal serviceResponseSpeed;

    @Schema(description = "综合评分")
    private BigDecimal totalScore;

    @Schema(description = "等级：1A级 2AA级 3AAA级")
    private Integer grade;

    @Schema(description = "等级名称")
    private String gradeName;

    @Schema(description = "排名")
    private Integer ranking;

    @Schema(description = "总供应商数")
    private Integer totalSuppliers;

    @Schema(description = "上期交付准时率")
    private BigDecimal previousDeliveryRate;

    @Schema(description = "上期质检合格率")
    private BigDecimal previousQualityRate;

    @Schema(description = "上期价格竞争力")
    private BigDecimal previousPriceScore;

    @Schema(description = "上期服务响应速度")
    private BigDecimal previousServiceScore;

    @Schema(description = "上期综合评分")
    private BigDecimal previousTotalScore;

    @Schema(description = "上期等级")
    private Integer previousGrade;

    @Schema(description = "上期排名")
    private Integer previousRanking;

    @Schema(description = "配额建议")
    private BigDecimal quotaSuggestion;

    @Schema(description = "优势分析")
    private String strengthAnalysis;

    @Schema(description = "劣势分析")
    private String weaknessAnalysis;

    @Schema(description = "改进建议")
    private String improvementSuggestion;

    @Schema(description = "统计开始日期")
    private LocalDate startDate;

    @Schema(description = "统计结束日期")
    private LocalDate endDate;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "审批人")
    private String approvedBy;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
