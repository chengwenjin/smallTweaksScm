package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "投标VO")
public class TenderBidVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "投标ID")
    private Long id;

    @Schema(description = "招标单ID")
    private Long tenderId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "投标价格")
    private BigDecimal bidPrice;

    @Schema(description = "投标时间")
    private LocalDateTime bidTime;

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

    @Schema(description = "是否中标：0否 1是")
    private Integer isWin;

    @Schema(description = "中标原因")
    private String winReason;

    @Schema(description = "得分")
    private Integer score;

    @Schema(description = "评标备注")
    private String evaluateRemark;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
