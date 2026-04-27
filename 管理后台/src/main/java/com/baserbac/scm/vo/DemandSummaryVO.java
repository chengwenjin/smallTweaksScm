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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "需求汇总信息")
public class DemandSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "汇总单编号")
    private String summaryNo;

    @Schema(description = "汇总单名称")
    private String summaryName;

    @Schema(description = "物料类别")
    private String materialCategory;

    @Schema(description = "周期类型：1月度 2季度 3年度")
    private Integer periodType;

    @Schema(description = "周期类型名称")
    private String periodTypeName;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "关联申请单数量")
    private Integer requestCount;

    @Schema(description = "物料种类数量")
    private Integer itemCount;

    @Schema(description = "总数量")
    private BigDecimal totalQuantity;

    @Schema(description = "预估金额")
    private BigDecimal estimatedAmount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "物料明细列表")
    private List<DemandSummaryItemVO> items;
}
