package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "供应商确认接单参数")
public class OrderConfirmDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(description = "供应商预计交货日期")
    private LocalDate supplierExpectedDeliveryDate;

    @NotBlank(message = "确认人不能为空")
    @Schema(description = "确认人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmedBy;

    @Schema(description = "确认备注")
    private String confirmRemark;
}
