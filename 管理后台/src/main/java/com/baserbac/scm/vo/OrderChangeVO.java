package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单变更记录信息")
public class OrderChangeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "变更单号")
    private String changeNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "变更类型：1数量变更 2价格变更 3交货日期变更 4其他变更")
    private Integer changeType;

    @Schema(description = "变更类型名称")
    private String changeTypeName;

    @Schema(description = "变更标题")
    private String changeTitle;

    @Schema(description = "变更原因")
    private String changeReason;

    @Schema(description = "原始内容（JSON）")
    private String originalContent;

    @Schema(description = "变更后内容（JSON）")
    private String changedContent;

    @Schema(description = "状态：0草稿 1待审批 2审批通过 3审批拒绝 4已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "审批人")
    private String approvalBy;

    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    @Schema(description = "审批备注")
    private String approvalRemark;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
