package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("scm_tender_bid")
public class TenderBid implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long tenderId;

    private Long supplierId;

    private String supplierName;

    private BigDecimal bidPrice;

    private LocalDateTime bidTime;

    private String bidDocs;

    private String bidDescription;

    private String technicalParams;

    private String deliveryPlan;

    private String afterSalesService;

    private Integer isWin;

    private String winReason;

    private Integer score;

    private String evaluateRemark;

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
