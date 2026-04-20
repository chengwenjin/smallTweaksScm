package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色创建请求
 */
@Data
@Schema(description = "角色创建请求")
public class RoleCreateDTO {

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", example = "系统管理员")
    private String roleName;

    @NotBlank(message = "角色标识不能为空")
    @Schema(description = "角色标识", example = "admin")
    private String roleKey;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "系统管理员角色")
    private String remark;
}
