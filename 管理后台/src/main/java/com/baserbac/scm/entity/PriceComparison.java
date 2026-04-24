package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_price_comparison")
public class PriceComparison implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long inquiryId;

    private String comparisonNo;

    private String comparisonName;

    private Long reqId;

    private String reqName;

    private String materialName;

    private String materialSpec;

    private String materialUnit;

    private BigDecimal reqQuantity;

    private Integer status;

    private Long recommendSupplierId;

    private String recommendSupplierName;

    private BigDecimal recommendPrice;

    private String recommendReason;

    private String comparisonResult;

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
