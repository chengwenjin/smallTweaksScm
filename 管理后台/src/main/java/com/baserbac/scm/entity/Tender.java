package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_tender")
public class Tender implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenderNo;

    private String tenderName;

    private Integer tenderType;

    private String description;

    private String reqIds;

    private BigDecimal estimatedBudget;

    private LocalDate publishDate;

    private LocalDate bidDeadline;

    private LocalDate openDate;

    private String contactPerson;

    private String contactPhone;

    private String contactEmail;

    private String tenderDocs;

    private String tenderAddress;

    private Integer status;

    private Long winSupplierId;

    private String winSupplierName;

    private BigDecimal winPrice;

    private String winReason;

    private LocalDate winDate;

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
