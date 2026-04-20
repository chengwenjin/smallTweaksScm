package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 分配角色请求
 */
@Data
@Schema(description = "分配角色请求")
public class AssignRoleDTO {

    @NotEmpty(message = "角色ID列表不能为空")
    @Schema(description = "角色ID列表", example = "[1, 2]")
    private List<Long> roleIds;
}
