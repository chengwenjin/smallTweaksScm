package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_material_inventory")
public class MaterialInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String materialCode;

    private String materialName;

    private String materialSpec;

    private String materialUnit;

    private String materialCategory;

    private BigDecimal stockQuantity;

    private BigDecimal availableQuantity;

    private BigDecimal reservedQuantity;

    private BigDecimal safetyStock;

    private BigDecimal minimumStock;

    private BigDecimal maximumStock;

    private String warehouseCode;

    private String warehouseName;

    private String locationCode;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private LocalDate lastInDate;

    private LocalDate lastOutDate;

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
