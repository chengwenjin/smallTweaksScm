package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "比价推荐参数")
public class PriceComparisonDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "比价单ID")
    private Long id;

    @Schema(description = "询价单ID")
    private Long inquiryId;

    @Schema(description = "采购需求单ID")
    private Long reqId;

    @Schema(description = "推荐供应商ID")
    private Long recommendSupplierId;

    @Schema(description = "推荐供应商名称")
    private String recommendSupplierName;

    @Schema(description = "推荐价格")
    private BigDecimal recommendPrice;

    @Schema(description = "推荐原因")
    private String recommendReason;

    @Schema(description = "比价结果")
    private String comparisonResult;

    @Schema(description = "备注")
    private String remark;
}
