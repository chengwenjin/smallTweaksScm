package com.baserbac.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单表实体
 */
@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父级ID，0为顶级
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 类型：1目录 2菜单 3按钮
     */
    private Integer menuType;

    /**
     * 权限标识(如sys:user:add)
     */
    private String permissionKey;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否在侧边栏显示：1显示 0隐藏
     */
    private Integer isVisible;

    /**
     * 是否缓存：1缓存 0不缓存
     */
    private Integer isCached;

    /**
     * 是否外链：1外链 0内链
     */
    private Integer isExternal;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 是否系统内置：1是
     */
    private Integer isSystem;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
