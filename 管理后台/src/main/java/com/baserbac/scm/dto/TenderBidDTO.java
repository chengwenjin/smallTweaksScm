package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "投标提交参数")
public class TenderBidDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "招标单ID")
    private Long tenderId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "投标价格")
    private BigDecimal bidPrice;

    @Schema(description = "投标文件URL列表")
    private String bidDocs;

    @Schema(description = "投标说明")
    private String bidDescription;

    @Schema(description = "技术参数")
    private String technicalParams;

    @Schema(description = "交货计划")
    private String deliveryPlan;

    @Schema(description = "售后服务")
    private String afterSalesService;

    @Schema(description = "备注")
    private String remark;
}
