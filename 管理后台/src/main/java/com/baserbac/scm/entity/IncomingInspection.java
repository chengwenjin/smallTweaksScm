package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_incoming_inspection")
public class IncomingInspection implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String inspectionNo;

    private Long orderId;

    private String orderNo;

    private Long shipmentId;

    private String shipmentNo;

    private Long appointmentId;

    private String appointmentNo;

    private Long supplierId;

    private String supplierName;

    private Integer inspectionType;

    private LocalDate inspectionDate;

    private String inspector;

    private String materialCode;

    private String materialName;

    private String materialSpec;

    private String materialUnit;

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private java.math.BigDecimal totalQuantity;

    private java.math.BigDecimal inspectionQuantity;

    private java.math.BigDecimal sampleQuantity;

    private java.math.BigDecimal passQuantity;

    private java.math.BigDecimal failQuantity;

    private Integer inspectionResult;

    private String passRate;

    private String inspectionItems;

    private String inspectionData;

    private String defectDescription;

    private String handlingSuggestion;

    private Integer handlingType;

    private Long warehouseReceiptId;

    private String warehouseReceiptNo;

    private String remark;

    private Integer status;

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
