package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色查询请求
 */
@Data
@Schema(description = "角色查询请求")
public class RoleQueryDTO {

    @Schema(description = "角色名称（模糊查询）", example = "管理员")
    private String roleName;

    @Schema(description = "角色标识（模糊查询）", example = "admin")
    private String roleKey;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private int pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private int pageSize = 10;
}
