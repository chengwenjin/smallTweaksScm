package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_purchase_order")
public class PurchaseOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private String orderName;

    private Long supplierId;

    private String supplierName;

    private String supplierCode;

    private Long sourceRequestId;

    private String sourceRequestNo;

    private Long approvalId;

    private String approvalNo;

    private Integer orderType;

    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    private LocalDate actualDeliveryDate;

    private Integer itemCount;

    private BigDecimal totalQuantity;

    private BigDecimal totalAmount;

    private String paymentTerms;

    private String deliveryTerms;

    private String deliveryAddress;

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
