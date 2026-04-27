package com.baserbac.scm.dto;

import com.baserbac.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "采购计划查询参数")
public class PurchasePlanQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "计划单编号")
    private String planNo;

    @Schema(description = "计划单名称")
    private String planName;

    @Schema(description = "计划类型：1月度计划 2季度计划 3年度计划 4紧急计划 5补货计划")
    private Integer planType;

    @Schema(description = "来源类型：1需求汇总 2生产工单 3安全库存 4人工创建")
    private String sourceType;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "状态")
    private String status;
}
