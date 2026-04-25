package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scm_supplier_performance_report")
public class SupplierPerformanceReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String reportNo;

    private String reportName;

    private Integer reportType;

    private Integer year;

    private Integer quarter;

    private Long supplierId;

    private String supplierName;

    private BigDecimal deliveryOnTimeRate;

    private BigDecimal qualityPassRate;

    private BigDecimal priceCompetitiveness;

    private BigDecimal serviceResponseSpeed;

    private BigDecimal totalScore;

    private Integer grade;

    private Integer ranking;

    private Integer totalSuppliers;

    private BigDecimal previousDeliveryRate;

    private BigDecimal previousQualityRate;

    private BigDecimal previousPriceScore;

    private BigDecimal previousServiceScore;

    private BigDecimal previousTotalScore;

    private Integer previousGrade;

    private Integer previousRanking;

    private BigDecimal quotaSuggestion;

    private String strengthAnalysis;

    private String weaknessAnalysis;

    private String improvementSuggestion;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer status;

    private String approvedBy;

    private LocalDateTime approveTime;

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
