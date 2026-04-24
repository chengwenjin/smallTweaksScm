package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "报价对比VO")
public class QuoteCompareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "总价")
    private BigDecimal totalPrice;

    @Schema(description = "与最低价差值")
    private BigDecimal priceDiff;

    @Schema(description = "价格排名")
    private Integer priceRank;

    @Schema(description = "交货日期")
    private LocalDate deliveryDate;

    @Schema(description = "付款条件")
    private String paymentTerms;

    @Schema(description = "质保期")
    private String warranty;

    @Schema(description = "历史合作次数")
    private Integer historyCooperationCount;

    @Schema(description = "历史平均交付天数")
    private Integer historyAvgDeliveryDays;

    @Schema(description = "综合得分")
    private BigDecimal totalScore;

    @Schema(description = "是否推荐")
    private Integer isRecommend;

    @Schema(description = "推荐原因")
    private String recommendReason;
}
