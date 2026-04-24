package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "询价供应商VO")
public class InquirySupplierVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "询价单ID")
    private Long inquiryId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "邀请状态：0待邀请 1已邀请 2已拒绝")
    private Integer inviteStatus;

    @Schema(description = "报价状态：0未报价 1已报价 2已撤回")
    private Integer quoteStatus;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "总价")
    private BigDecimal totalPrice;

    @Schema(description = "交货日期")
    private LocalDate deliveryDate;

    @Schema(description = "付款条件")
    private String paymentTerms;

    @Schema(description = "质保期")
    private String warranty;

    @Schema(description = "报价说明")
    private String quoteRemark;

    @Schema(description = "报价时间")
    private LocalDateTime quoteTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
