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
@Schema(description = "采购计划明细信息")
public class PurchasePlanItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "采购计划ID")
    private Long planId;

    @Schema(description = "物料编码")
    private String materialCode;

    @Schema(description = "物料名称")
    private String materialName;

    @Schema(description = "物料规格")
    private String materialSpec;

    @Schema(description = "物料单位")
    private String materialUnit;

    @Schema(description = "物料类别")
    private String materialCategory;

    @Schema(description = "需求数量")
    private BigDecimal requiredQuantity;

    @Schema(description = "现有库存")
    private BigDecimal stockQuantity;

    @Schema(description = "安全库存")
    private BigDecimal safetyStock;

    @Schema(description = "缺货数量")
    private BigDecimal shortageQuantity;

    @Schema(description = "建议采购数量")
    private BigDecimal purchaseQuantity;

    @Schema(description = "参考单价")
    private BigDecimal unitPrice;

    @Schema(description = "预估金额")
    private BigDecimal estimatedAmount;

    @Schema(description = "推荐供应商ID")
    private Long recommendedSupplierId;

    @Schema(description = "推荐供应商名称")
    private String recommendedSupplierName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
