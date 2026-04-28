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
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "送货预约信息")
public class DeliveryAppointmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "预约单号")
    private String appointmentNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "发货记录ID")
    private Long shipmentId;

    @Schema(description = "发货单号")
    private String shipmentNo;

    @Schema(description = "预约日期")
    private LocalDate appointmentDate;

    @Schema(description = "预约开始时间")
    private LocalTime appointmentTimeFrom;

    @Schema(description = "预约结束时间")
    private LocalTime appointmentTimeTo;

    @Schema(description = "时间段")
    private String timeSlot;

    @Schema(description = "送货地址")
    private String deliveryAddress;

    @Schema(description = "仓库ID")
    private Integer warehouseId;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "码头编号")
    private Integer dockNo;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "物料种类数量")
    private Integer itemCount;

    @Schema(description = "总数量")
    private BigDecimal totalQuantity;

    @Schema(description = "总重量")
    private BigDecimal totalWeight;

    @Schema(description = "总体积")
    private BigDecimal totalVolume;

    @Schema(description = "车牌号")
    private String vehicleNo;

    @Schema(description = "司机姓名")
    private String driverName;

    @Schema(description = "司机电话")
    private String driverPhone;

    @Schema(description = "状态：0待确认 1已确认 2已签到 3已完成 4已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;

    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;

    @Schema(description = "仓库操作员")
    private String warehouseOperator;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
