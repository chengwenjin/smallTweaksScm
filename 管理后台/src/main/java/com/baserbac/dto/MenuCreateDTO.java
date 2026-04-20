package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单创建请求
 */
@Data
@Schema(description = "菜单创建请求")
public class MenuCreateDTO {

    @NotNull(message = "父菜单ID不能为空")
    @Schema(description = "父菜单ID，0表示顶级", example = "0")
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    @NotNull(message = "菜单类型不能为空")
    @Schema(description = "菜单类型：1-目录 2-菜单 3-按钮", example = "2")
    private Integer menuType;

    @Schema(description = "权限标识", example = "system:user:list")
    private String permissionKey;

    @Schema(description = "路由路径", example = "/system/user")
    private String path;

    @Schema(description = "组件路径", example = "system/user/index")
    private String component;

    @Schema(description = "菜单图标", example = "user")
    private String icon;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否可见：0-隐藏 1-显示", example = "1")
    private Integer isVisible;

    @Schema(description = "是否缓存：0-不缓存 1-缓存", example = "1")
    private Integer isCached;

    @Schema(description = "是否外链：0-否 1-是", example = "0")
    private Integer isExternal;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "用户管理菜单")
    private String remark;
}
