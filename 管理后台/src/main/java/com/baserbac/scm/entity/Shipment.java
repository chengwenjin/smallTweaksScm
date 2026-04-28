package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_shipment")
public class Shipment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String shipmentNo;

    private Long orderId;

    private String orderNo;

    private Long supplierId;

    private String supplierName;

    private Integer shipmentType;

    private LocalDate shipmentDate;

    private LocalDate estimatedArrivalDate;

    private LocalDate actualArrivalDate;

    private Integer itemCount;

    private java.math.BigDecimal totalQuantity;

    private java.math.BigDecimal totalWeight;

    private String shippingMethod;

    private String carrier;

    private String waybillNo;

    private String departurePlace;

    private String destination;

    private String contactPerson;

    private String contactPhone;

    private Integer status;

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
