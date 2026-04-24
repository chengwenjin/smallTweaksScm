package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "采购需求单查询参数")
public class PurchaseRequirementQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "需求单编号")
    private String reqNo;

    @Schema(description = "需求单名称")
    private String reqName;

    @Schema(description = "物资名称")
    private String materialName;

    @Schema(description = "状态：0待审批 1审批通过 2已询价 3已比价 4已采购 5已完成 6已取消")
    private Integer status;

    @Schema(description = "紧急程度：1普通 2紧急 3特急")
    private Integer urgency;

    @Schema(description = "需求部门")
    private String reqDept;
}
