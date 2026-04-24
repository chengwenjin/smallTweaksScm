package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "列入黑名单参数")
public class BlacklistCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "供应商ID不能为空")
    @Schema(description = "供应商ID", required = true)
    private Long supplierId;

    @NotNull(message = "黑名单类型不能为空")
    @Schema(description = "黑名单类型：1严重违约 2质量问题 3欺诈行为 4其他", required = true, example = "1")
    private Integer blacklistType;

    @NotBlank(message = "列入原因不能为空")
    @Schema(description = "列入黑名单原因", required = true)
    private String blacklistReason;

    @Schema(description = "是否永久：0否 1是", example = "0")
    private Integer isPermanent = 0;

    @Schema(description = "到期日期（非永久时必填）")
    private LocalDate expireDate;

    @Schema(description = "备注")
    private String remark;
}
