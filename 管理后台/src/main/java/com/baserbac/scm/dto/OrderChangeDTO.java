package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "订单变更参数")
public class OrderChangeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @NotNull(message = "变更类型不能为空")
    @Schema(description = "变更类型：1交货日期变更 2数量变更 3价格变更 4其他变更", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer changeType;

    @NotBlank(message = "变更原因不能为空")
    @Schema(description = "变更原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String changeReason;

    @Schema(description = "新的期望交货日期")
    private LocalDate newExpectedDeliveryDate;

    @Schema(description = "新的数量")
    private BigDecimal newQuantity;

    @Schema(description = "新的单价")
    private BigDecimal newUnitPrice;

    @Schema(description = "变更说明")
    private String changeDescription;
}
