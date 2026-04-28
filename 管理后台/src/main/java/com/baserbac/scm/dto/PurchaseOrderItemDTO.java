package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "采购订单明细参数")
public class PurchaseOrderItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "明细ID（更新时使用）")
    private Long id;

    @NotBlank(message = "物料编码不能为空")
    @Schema(description = "物料编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialCode;

    @NotBlank(message = "物料名称不能为空")
    @Schema(description = "物料名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialName;

    @Schema(description = "物料规格")
    private String materialSpec;

    @Schema(description = "物料单位")
    private String materialUnit;

    @Schema(description = "物料类别")
    private String materialCategory;

    @NotNull(message = "数量不能为空")
    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantity;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "备注")
    private String remark;
}
