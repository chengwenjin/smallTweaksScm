package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜单查询请求
 */
@Data
@Schema(description = "菜单查询请求")
public class MenuQueryDTO {

    @Schema(description = "菜单名称（模糊查询）", example = "用户管理")
    private String menuName;

    @Schema(description = "菜单类型：1-目录 2-菜单 3-按钮", example = "1")
    private Integer menuType;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;
}
