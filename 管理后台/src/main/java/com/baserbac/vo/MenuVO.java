package com.baserbac.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单信息VO（树形）
 */
@Data
@Schema(description = "菜单信息")
public class MenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "类型：1目录 2菜单 3按钮")
    private Integer menuType;

    @Schema(description = "权限标识")
    private String permissionKey;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否显示")
    private Integer isVisible;

    @Schema(description = "是否缓存")
    private Integer isCached;

    @Schema(description = "是否外链")
    private Integer isExternal;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子菜单列表")
    private List<MenuVO> children;
}
