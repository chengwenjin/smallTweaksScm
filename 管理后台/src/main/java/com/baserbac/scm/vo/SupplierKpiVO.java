package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "供应商KPI信息")
public class SupplierKpiVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "KPI记录ID")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "周期类型：1月度 2季度 3年度")
    private Integer periodType;

    @Schema(description = "周期类型名称")
    private String periodTypeName;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "季度")
    private Integer quarter;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "交付准时率")
    private BigDecimal deliveryOnTimeRate;

    @Schema(description = "交付总次数")
    private Integer deliveryTotalCount;

    @Schema(description = "交付准时次数")
    private Integer deliveryOnTimeCount;

    @Schema(description = "来料质检合格率")
    private BigDecimal qualityPassRate;

    @Schema(description = "质检总次数")
    private Integer qualityTotalCount;

    @Schema(description = "质检合格次数")
    private Integer qualityPassCount;

    @Schema(description = "价格竞争力")
    private BigDecimal priceCompetitiveness;

    @Schema(description = "价格对比次数")
    private Integer priceCompareCount;

    @Schema(description = "价格最优次数")
    private Integer priceBestCount;

    @Schema(description = "售后服务响应速度")
    private BigDecimal serviceResponseSpeed;

    @Schema(description = "服务总次数")
    private Integer serviceTotalCount;

    @Schema(description = "服务响应及时次数")
    private Integer serviceResponseOnTimeCount;

    @Schema(description = "综合评分")
    private BigDecimal totalScore;

    @Schema(description = "等级：1A级 2AA级 3AAA级")
    private Integer grade;

    @Schema(description = "等级名称")
    private String gradeName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
