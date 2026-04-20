package com.baserbac.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 接口权限表实体
 */
@Data
@TableName("sys_api_permission")
public class SysApiPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限标识，与菜单/按钮对应
     */
    private String permissionKey;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 请求方式：GET/POST/PUT/DELETE
     */
    private String requestMethod;

    /**
     * 接口路径
     */
    private String apiPath;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;

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
