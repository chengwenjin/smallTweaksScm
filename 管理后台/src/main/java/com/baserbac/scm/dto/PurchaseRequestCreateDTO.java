package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "采购申请创建参数")
public class PurchaseRequestCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "申请单标题")
    @NotBlank(message = "申请单标题不能为空")
    private String reqTitle;

    @Schema(description = "申请部门")
    private String reqDept;

    @Schema(description = "申请人")
    private String reqPerson;

    @Schema(description = "联系电话")
    private String reqPhone;

    @Schema(description = "需求日期")
    private LocalDate requiredDate;

    @Schema(description = "交货地址")
    private String deliveryAddress;

    @Schema(description = "紧急程度：1普通 2紧急 3特急")
    private Integer urgency;

    @Schema(description = "预算来源")
    private String budgetSource;

    @Schema(description = "需求描述")
    private String description;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "物料明细列表")
    private List<PurchaseRequestItemDTO> items;
}
