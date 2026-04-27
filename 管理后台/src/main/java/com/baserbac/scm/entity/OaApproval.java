package com.baserbac.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("scm_oa_approval")
public class OaApproval implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String approvalNo;

    private Integer sourceType;

    private Long sourceId;

    private String sourceNo;

    private String approvalTitle;

    private String currentApproverId;

    private String currentApproverName;

    private String approvalStatus;

    private LocalDateTime submitTime;

    private LocalDateTime approvalTime;

    private String approvalRemark;

    private String approvalHistory;

    private String remark;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
