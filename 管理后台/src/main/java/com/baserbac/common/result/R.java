package com.baserbac.common.result;

import com.baserbac.common.enums.ResultCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    public R() {
    }

    public R(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> R<T> success() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> success(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> R<T> success(String message, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     */
    public static <T> R<T> error(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> R<T> error(Integer code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 失败响应（使用ResultCode，自定义消息）
     */
    public static <T> R<T> error(ResultCode resultCode, String message) {
        return new R<>(resultCode.getCode(), message, null);
    }
}
