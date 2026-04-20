package com.baserbac.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色表实体
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色标识(权限编码)，不可修改
     */
    private String roleKey;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 是否系统内置：1是
     */
    private Integer isSystem;

    /**
     * 备注
     */
    private String remark;

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

    /**
     * 预留扩展字段1
     */
    private String ext1;

    /**
     * 预留扩展字段2
     */
    private String ext2;

    /**
     * 预留扩展字段3
     */
    private String ext3;
}
