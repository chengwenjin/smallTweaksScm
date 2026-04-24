package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_inquiry_supplier")
public class InquirySupplier implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long inquiryId;

    private Long supplierId;

    private String supplierName;

    private Integer inviteStatus;

    private Integer quoteStatus;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private LocalDate deliveryDate;

    private String paymentTerms;

    private String warranty;

    private String quoteRemark;

    private LocalDateTime quoteTime;

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
