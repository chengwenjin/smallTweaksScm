package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OA审批信息")
public class OaApprovalVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "审批单编号")
    private String approvalNo;

    @Schema(description = "来源类型：1采购申请 2采购计划 3采购订单")
    private Integer sourceType;

    @Schema(description = "来源类型名称")
    private String sourceTypeName;

    @Schema(description = "来源单据ID")
    private Long sourceId;

    @Schema(description = "来源单据编号")
    private String sourceNo;

    @Schema(description = "审批标题")
    private String approvalTitle;

    @Schema(description = "当前审批人ID")
    private String currentApproverId;

    @Schema(description = "当前审批人名称")
    private String currentApproverName;

    @Schema(description = "审批状态：0待提交 1审批中 2审批通过 3审批拒绝 4已撤回")
    private String approvalStatus;

    @Schema(description = "审批状态名称")
    private String approvalStatusName;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    @Schema(description = "审批备注")
    private String approvalRemark;

    @Schema(description = "审批历史记录")
    private String approvalHistory;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
