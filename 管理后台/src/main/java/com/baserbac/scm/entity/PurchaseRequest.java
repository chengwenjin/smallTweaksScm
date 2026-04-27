package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_purchase_request")
public class PurchaseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String reqNo;

    private String reqTitle;

    private String reqDept;

    private String reqPerson;

    private String reqPhone;

    private LocalDate requiredDate;

    private String deliveryAddress;

    private Integer urgency;

    private BigDecimal totalAmount;

    private String budgetSource;

    private String description;

    private Integer status;

    private String approvalStatus;

    private String currentApprover;

    private LocalDateTime submitTime;

    private LocalDateTime approvalTime;

    private String approvalRemark;

    private Long generatedOrderId;

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
