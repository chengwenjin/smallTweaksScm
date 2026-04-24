package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_purchase_requirement")
public class PurchaseRequirement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String reqNo;

    private String reqName;

    private String materialName;

    private String materialSpec;

    private String materialUnit;

    private BigDecimal quantity;

    private LocalDate requiredDate;

    private Integer urgency;

    private String budgetRange;

    private String description;

    private Integer status;

    private String reqDept;

    private String reqPerson;

    private String reqPhone;

    private LocalDate deliveryDate;

    private String deliveryAddress;

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
