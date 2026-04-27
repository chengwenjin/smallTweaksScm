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
@Schema(description = "采购计划信息")
public class PurchasePlanVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "计划单编号")
    private String planNo;

    @Schema(description = "计划单名称")
    private String planName;

    @Schema(description = "计划类型：1月度计划 2季度计划 3年度计划 4紧急计划 5补货计划")
    private Integer planType;

    @Schema(description = "计划类型名称")
    private String planTypeName;

    @Schema(description = "来源类型：1需求汇总 2生产工单 3安全库存 4人工创建")
    private String sourceType;

    @Schema(description = "来源类型名称")
    private String sourceTypeName;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "季度")
    private Integer quarter;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

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
    private List<PurchasePlanItemVO> items;
}
