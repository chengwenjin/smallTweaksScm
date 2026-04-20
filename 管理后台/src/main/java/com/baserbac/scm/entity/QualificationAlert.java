package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资质预警记录实体
 */
@Data
@TableName("scm_qualification_alert")
public class QualificationAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long qualificationId;

    private Long supplierId;

    private Integer alertType;

    private String alertTitle;

    private String alertContent;

    private LocalDate alertDate;

    private Integer daysBeforeExpiry;

    private Integer isRead;

    private LocalDateTime readTime;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
