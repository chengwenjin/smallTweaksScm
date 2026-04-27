package com.baserbac.scm.dto;

import com.baserbac.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "需求汇总查询参数")
public class DemandSummaryQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "汇总单编号")
    private String summaryNo;

    @Schema(description = "汇总单名称")
    private String summaryName;

    @Schema(description = "物料类别")
    private String materialCategory;

    @Schema(description = "周期类型：1月度 2季度 3年度")
    private Integer periodType;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "状态")
    private String status;
}
