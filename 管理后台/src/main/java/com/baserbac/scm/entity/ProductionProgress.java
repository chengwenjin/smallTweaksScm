package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_production_progress")
public class ProductionProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String orderNo;

    private Long orderItemId;

    private String materialCode;

    private String materialName;

    private String materialSpec;

    private String materialUnit;

    private java.math.BigDecimal totalQuantity;

    private java.math.BigDecimal completedQuantity;

    private java.math.BigDecimal progressRate;

    private Integer progressStatus;

    private String workStation;

    private String responsiblePerson;

    private LocalDate estimatedStartDate;

    private LocalDate actualStartDate;

    private LocalDate estimatedFinishDate;

    private LocalDate actualFinishDate;

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
