package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色状态变更请求
 */
@Data
@Schema(description = "角色状态变更请求")
public class RoleStatusDTO {

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}
