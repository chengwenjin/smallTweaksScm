package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_supplier_blacklist")
public class SupplierBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long supplierId;

    private String supplierCode;

    private String supplierName;

    private Integer blacklistType;

    private String blacklistReason;

    private LocalDate blacklistDate;

    private Integer isPermanent;

    private LocalDate expireDate;

    private Integer status;

    private String removeReason;

    private LocalDate removeDate;

    @TableLogic
    private Integer isDeleted;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
