package com.baserbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志表实体
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private Integer loginStatus;

    private String failReason;

    private String ip;

    private String location;

    private String browser;

    private String os;

    private String userAgent;

    private LocalDateTime loginTime;
}
