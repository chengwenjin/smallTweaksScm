package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "报价提交参数")
public class QuoteSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "询价单ID")
    private Long inquiryId;

    @Schema(description = "供应商ID")
    private Long supplierId;

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
}
