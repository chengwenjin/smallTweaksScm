package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "采购订单信息")
public class PurchaseOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "订单名称")
    private String orderName;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "来源采购申请ID")
    private Long sourceRequestId;

    @Schema(description = "来源采购申请编号")
    private String sourceRequestNo;

    @Schema(description = "审批单ID")
    private Long approvalId;

    @Schema(description = "审批单编号")
    private String approvalNo;

    @Schema(description = "订单类型：1标准订单 2紧急订单 3补货订单")
    private Integer orderType;

    @Schema(description = "订单类型名称")
    private String orderTypeName;

    @Schema(description = "订单日期")
    private LocalDate orderDate;

    @Schema(description = "期望交货日期")
    private LocalDate expectedDeliveryDate;

    @Schema(description = "实际交货日期")
    private LocalDate actualDeliveryDate;

    @Schema(description = "物料种类数量")
    private Integer itemCount;

    @Schema(description = "总数量")
    private BigDecimal totalQuantity;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

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

    @Schema(description = "状态：0新建 1待审批 2审批通过 3已发布 4部分收货 5全部收货 6已完成 7已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
