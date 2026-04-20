package com.baserbac.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 请求参数错误
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 未认证
     */
    UNAUTHORIZED(401, "登录已过期，请重新登录"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无操作权限"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 服务器内部错误
     */
    INTERNAL_ERROR(500, "服务器内部错误"),

    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(4001, "用户名已存在"),

    /**
     * 角色标识已存在
     */
    ROLE_KEY_EXISTS(4002, "角色标识已存在"),

    /**
     * 权限标识已存在
     */
    PERMISSION_KEY_EXISTS(4003, "权限标识已存在"),

    /**
     * 菜单存在子菜单
     */
    MENU_HAS_CHILDREN(4005, "菜单存在子菜单，无法删除"),

    /**
     * 验证码错误或已过期
     */
    CAPTCHA_ERROR(4004, "验证码错误或已过期"),

    /**
     * 验证码已过期
     */
    CAPTCHA_EXPIRED(4010, "验证码已过期"),

    /**
     * Token无效或已过期
     */
    TOKEN_INVALID(4011, "Token无效或已过期"),

    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(4005, "用户名或密码错误"),

    /**
     * 账号已被禁用
     */
    ACCOUNT_DISABLED(4006, "账号已被禁用"),

    /**
     * 账号已被锁定
     */
    ACCOUNT_LOCKED(4007, "账号已被锁定，请{0}分钟后再试"),

    /**
     * 原密码错误
     */
    OLD_PASSWORD_ERROR(4008, "原密码错误"),

    /**
     * 系统内置数据不可操作
     */
    SYSTEM_DATA_PROTECTED(4009, "系统内置数据不可操作"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(4012, "用户不存在");

    private final Integer code;
    private final String message;
}
