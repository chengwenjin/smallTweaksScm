package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "比价单VO")
public class PriceComparisonVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "比价单ID")
    private Long id;

    @Schema(description = "询价单ID")
    private Long inquiryId;

    @Schema(description = "比价单编号")
    private String comparisonNo;

    @Schema(description = "比价单名称")
    private String comparisonName;

    @Schema(description = "采购需求单ID")
    private Long reqId;

    @Schema(description = "采购需求单名称")
    private String reqName;

    @Schema(description = "物资名称")
    private String materialName;

    @Schema(description = "物资规格")
    private String materialSpec;

    @Schema(description = "物资单位")
    private String materialUnit;

    @Schema(description = "需求数量")
    private BigDecimal reqQuantity;

    @Schema(description = "状态：0待比价 1比价中 2已完成 3已取消")
    private Integer status;

    @Schema(description = "推荐供应商ID")
    private Long recommendSupplierId;

    @Schema(description = "推荐供应商名称")
    private String recommendSupplierName;

    @Schema(description = "推荐价格")
    private BigDecimal recommendPrice;

    @Schema(description = "推荐原因")
    private String recommendReason;

    @Schema(description = "比价结果")
    private String comparisonResult;

    @Schema(description = "报价对比列表")
    private List<QuoteCompareVO> quoteCompares;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
