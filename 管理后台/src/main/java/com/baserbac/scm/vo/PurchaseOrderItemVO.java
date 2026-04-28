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
@Schema(description = "采购订单明细信息")
public class PurchaseOrderItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "物料编码")
    private String materialCode;

    @Schema(description = "物料名称")
    private String materialName;

    @Schema(description = "规格型号")
    private String materialSpec;

    @Schema(description = "单位")
    private String materialUnit;

    @Schema(description = "物料类别")
    private String materialCategory;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "总价")
    private BigDecimal totalPrice;

    @Schema(description = "已接收数量")
    private BigDecimal receivedQuantity;

    @Schema(description = "已检验数量")
    private BigDecimal inspectedQuantity;

    @Schema(description = "合格数量")
    private BigDecimal passQuantity;

    @Schema(description = "不合格数量")
    private BigDecimal failQuantity;

    @Schema(description = "期望交货日期")
    private LocalDateTime expectedDeliveryDate;

    @Schema(description = "实际交货日期")
    private LocalDateTime actualDeliveryDate;

    @Schema(description = "状态")
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
