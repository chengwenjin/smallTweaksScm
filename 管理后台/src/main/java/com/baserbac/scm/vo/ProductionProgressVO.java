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
@Schema(description = "生产进度信息")
public class ProductionProgressVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "订单明细ID")
    private Long orderItemId;

    @Schema(description = "物料编码")
    private String materialCode;

    @Schema(description = "物料名称")
    private String materialName;

    @Schema(description = "规格型号")
    private String materialSpec;

    @Schema(description = "单位")
    private String materialUnit;

    @Schema(description = "总数量")
    private BigDecimal totalQuantity;

    @Schema(description = "已完成数量")
    private BigDecimal completedQuantity;

    @Schema(description = "进度比例")
    private BigDecimal progressRate;

    @Schema(description = "进度状态：0待开始 1进行中 2已完成 3已暂停 4已延误")
    private Integer progressStatus;

    @Schema(description = "进度状态名称")
    private String progressStatusName;

    @Schema(description = "工位")
    private String workStation;

    @Schema(description = "负责人")
    private String responsiblePerson;

    @Schema(description = "计划开始日期")
    private LocalDate estimatedStartDate;

    @Schema(description = "实际开始日期")
    private LocalDate actualStartDate;

    @Schema(description = "计划完成日期")
    private LocalDate estimatedFinishDate;

    @Schema(description = "实际完成日期")
    private LocalDate actualFinishDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
