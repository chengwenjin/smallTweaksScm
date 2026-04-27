package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("scm_bom")
public class Bom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String bomNo;

    private String bomName;

    private Integer bomVersion;

    private String parentCode;

    private String parentName;

    private String parentSpec;

    private String materialCode;

    private String materialName;

    private String materialSpec;

    private String materialUnit;

    private String materialCategory;

    private BigDecimal usageQuantity;

    private BigDecimal scrapRate;

    private Integer sortOrder;

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
