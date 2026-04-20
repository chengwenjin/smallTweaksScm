package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 资质审核DTO
 */
@Data
@Schema(description = "资质审核参数")
public class QualificationAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：2审核通过 3审核拒绝", example = "2")
    private Integer auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;
}
