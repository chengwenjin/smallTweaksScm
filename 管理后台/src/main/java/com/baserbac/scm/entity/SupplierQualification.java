package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商资质审核实体
 */
@Data
@TableName("scm_supplier_qualification")
public class SupplierQualification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long supplierId;

    private String qualificationType;

    private String qualificationName;

    private String certificateNo;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private Integer isLongTerm;

    private String fileUrls;

    private String issuingAuthority;

    private Integer auditStatus;

    private String auditRemark;

    private String auditBy;

    private LocalDateTime auditTime;

    private Integer alertStatus;

    private Integer alertSent;

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
