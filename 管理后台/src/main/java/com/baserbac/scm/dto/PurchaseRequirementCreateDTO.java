package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "采购需求单创建参数")
public class PurchaseRequirementCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "需求单名称不能为空")
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
}
