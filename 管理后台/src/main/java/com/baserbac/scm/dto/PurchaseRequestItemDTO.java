package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "采购申请明细参数")
public class PurchaseRequestItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "物料编码")
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;

    @Schema(description = "物料名称")
    @NotBlank(message = "物料名称不能为空")
    private String materialName;

    @Schema(description = "物料规格")
    private String materialSpec;

    @Schema(description = "物料单位")
    @NotBlank(message = "物料单位不能为空")
    private String materialUnit;

    @Schema(description = "物料类别")
    private String materialCategory;

    @Schema(description = "需求数量")
    @NotNull(message = "需求数量不能为空")
    private BigDecimal quantity;

    @Schema(description = "参考单价")
    private BigDecimal unitPrice;

    @Schema(description = "备注")
    private String remark;
}
