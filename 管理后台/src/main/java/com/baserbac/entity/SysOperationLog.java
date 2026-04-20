package com.baserbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志表实体
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作类型：1登录 2操作 3异常
     */
    private Integer logType;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人用户名
     */
    private String operatorName;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数(JSON)
     */
    private String requestParams;

    /**
     * 请求头信息(脱敏)
     */
    private String requestHeaders;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 响应结果摘要(JSON)
     */
    private String responseResult;

    /**
     * 请求IP
     */
    private String ip;

    /**
     * 浏览器User-Agent
     */
    private String userAgent;

    /**
     * 操作状态：1成功 0失败
     */
    private Integer status;

    /**
     * 异常信息(失败时)
     */
    private String errorMsg;

    /**
     * 操作耗时(ms)
     */
    private Integer duration;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;
}
