package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "采购订单创建参数")
public class PurchaseOrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "订单名称不能为空")
    @Schema(description = "订单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderName;

    @NotNull(message = "供应商ID不能为空")
    @Schema(description = "供应商ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long supplierId;

    @Schema(description = "订单类型：1标准订单 2紧急订单 3补货订单")
    private Integer orderType = 1;

    @Schema(description = "订单日期")
    private LocalDate orderDate;

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

    @Schema(description = "来源采购申请ID")
    private Long sourceRequestId;

    @Schema(description = "备注")
    private String remark;

    @NotEmpty(message = "订单明细不能为空")
    @Schema(description = "订单明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PurchaseOrderItemDTO> items;
}
