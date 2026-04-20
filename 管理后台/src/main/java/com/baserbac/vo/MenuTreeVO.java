package com.baserbac.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单树形VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜单树形信息")
public class MenuTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单类型：1-目录 2-菜单 3-按钮")
    private Integer menuType;

    @Schema(description = "权限标识")
    private String permissionKey;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否可见：0-隐藏 1-显示")
    private Integer isVisible;

    @Schema(description = "是否缓存：0-不缓存 1-缓存")
    private Integer isCached;

    @Schema(description = "是否外链：0-否 1-是")
    private Integer isExternal;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子菜单列表")
    private List<MenuTreeVO> children;
}
