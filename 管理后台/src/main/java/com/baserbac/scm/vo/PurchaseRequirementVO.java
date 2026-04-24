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
@Schema(description = "采购需求单VO")
public class PurchaseRequirementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "需求单ID")
    private Long id;

    @Schema(description = "需求单编号")
    private String reqNo;

    @Schema(description = "需求单名称")
    private String reqName;

    @Schema(description = "物资名称")
    private String materialName;

    @Schema(description = "物资规格")
    private String materialSpec;

    @Schema(description = "物资单位")
    private String materialUnit;

    @Schema(description = "需求数量")
    private BigDecimal quantity;

    @Schema(description = "需求日期")
    private LocalDate requiredDate;

    @Schema(description = "紧急程度：1普通 2紧急 3特急")
    private Integer urgency;

    @Schema(description = "预算范围")
    private String budgetRange;

    @Schema(description = "需求描述")
    private String description;

    @Schema(description = "状态：0待审批 1审批通过 2已询价 3已比价 4已采购 5已完成 6已取消")
    private Integer status;

    @Schema(description = "需求部门")
    private String reqDept;

    @Schema(description = "需求人")
    private String reqPerson;

    @Schema(description = "联系电话")
    private String reqPhone;

    @Schema(description = "期望交货日期")
    private LocalDate deliveryDate;

    @Schema(description = "交货地址")
    private String deliveryAddress;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
