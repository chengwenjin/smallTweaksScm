package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "采购订单更新参数")
public class PurchaseOrderUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "订单名称")
    private String orderName;

    @Schema(description = "订单类型：1标准订单 2紧急订单 3补货订单")
    private Integer orderType;

    @Schema(description = "期望交货日期")
    private LocalDate expectedDeliveryDate;

    @Schema(description = "付款条款")
    private String paymentTerms;

    @Schema(description = "交货条款")
    private String deliveryTerms;

    @Schema(description = "交货地址")
    private String deliveryAddress;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "备注")
    private String remark;
}
