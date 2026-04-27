package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "生成需求汇总参数")
public class GenerateSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "物料类别")
    private String materialCategory;

    @Schema(description = "周期类型：1月度 2季度 3年度")
    private Integer periodType;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "汇总名称")
    private String summaryName;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "备注")
    private String remark;
}
