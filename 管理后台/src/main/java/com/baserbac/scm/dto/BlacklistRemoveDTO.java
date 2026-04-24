package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "移除黑名单参数")
public class BlacklistRemoveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "移除原因不能为空")
    @Schema(description = "移除原因", required = true)
    private String removeReason;
}
