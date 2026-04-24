package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "定标参数")
public class TenderAwardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "招标单ID")
    private Long tenderId;

    @Schema(description = "中标供应商ID")
    private Long winSupplierId;

    @Schema(description = "中标供应商名称")
    private String winSupplierName;

    @Schema(description = "中标价格")
    private BigDecimal winPrice;

    @Schema(description = "中标原因")
    private String winReason;

    @Schema(description = "中标日期")
    private LocalDate winDate;
}
