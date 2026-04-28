package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "订单取消参数")
public class OrderCancelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @NotBlank(message = "取消原因不能为空")
    @Schema(description = "取消原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cancelReason;

    @Schema(description = "取消备注")
    private String cancelRemark;
}
