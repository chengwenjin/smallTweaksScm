package com.baserbac.scm.dto;

import com.baserbac.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "采购申请查询参数")
public class PurchaseRequestQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "申请单编号")
    private String reqNo;

    @Schema(description = "申请单标题")
    private String reqTitle;

    @Schema(description = "申请部门")
    private String reqDept;

    @Schema(description = "申请人")
    private String reqPerson;

    @Schema(description = "紧急程度：1普通 2紧急 3特急")
    private Integer urgency;

    @Schema(description = "状态：0草稿 1待提交 2审批中 3审批通过 4审批拒绝 5已转订单 6已取消")
    private Integer status;

    @Schema(description = "审批状态")
    private String approvalStatus;
}
