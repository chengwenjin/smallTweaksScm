package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("scm_supplier_kpi")
public class SupplierKpi implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long supplierId;

    private String supplierName;

    private Integer periodType;

    private Integer year;

    private Integer quarter;

    private Integer month;

    private BigDecimal deliveryOnTimeRate;

    private Integer deliveryTotalCount;

    private Integer deliveryOnTimeCount;

    private BigDecimal qualityPassRate;

    private Integer qualityTotalCount;

    private Integer qualityPassCount;

    private BigDecimal priceCompetitiveness;

    private Integer priceCompareCount;

    private Integer priceBestCount;

    private BigDecimal serviceResponseSpeed;

    private Integer serviceTotalCount;

    private Integer serviceResponseOnTimeCount;

    private BigDecimal totalScore;

    private Integer grade;

    private Integer status;

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
