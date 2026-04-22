package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("scm_supplier_classification_log")
public class SupplierClassificationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long supplierId;

    private Integer oldMaterialCategory;

    private Integer newMaterialCategory;

    private Integer oldCooperationLevel;

    private Integer newCooperationLevel;

    private String changeReason;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
