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
@Schema(description = "采购申请信息")
public class PurchaseRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "申请单编号")
    private String reqNo;

    @Schema(description = "申请单标题")
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

    @Schema(description = "紧急程度名称")
    private String urgencyName;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "预算来源")
    private String budgetSource;

    @Schema(description = "需求描述")
    private String description;

    @Schema(description = "状态：0草稿 1待提交 2审批中 3审批通过 4审批拒绝 5已转订单 6已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "审批状态")
    private String approvalStatus;

    @Schema(description = "当前审批人")
    private String currentApprover;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    @Schema(description = "审批备注")
    private String approvalRemark;

    @Schema(description = "生成的采购订单ID")
    private Long generatedOrderId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "物料明细列表")
    private List<PurchaseRequestItemVO> items;
}
