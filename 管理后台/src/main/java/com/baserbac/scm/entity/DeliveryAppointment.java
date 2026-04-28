package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("scm_delivery_appointment")
public class DeliveryAppointment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String appointmentNo;

    private Long orderId;

    private String orderNo;

    private Long supplierId;

    private String supplierName;

    private Long shipmentId;

    private String shipmentNo;

    private LocalDate appointmentDate;

    private LocalTime appointmentTimeFrom;

    private LocalTime appointmentTimeTo;

    private String deliveryAddress;

    private Integer warehouseId;

    private String warehouseName;

    private Integer dockNo;

    private String contactPerson;

    private String contactPhone;

    private Integer itemCount;

    private java.math.BigDecimal totalQuantity;

    private java.math.BigDecimal totalWeight;

    private java.math.BigDecimal totalVolume;

    private String vehicleNo;

    private String driverName;

    private String driverPhone;

    private Integer status;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private String warehouseOperator;

    private String remark;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
