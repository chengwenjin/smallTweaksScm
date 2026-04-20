package com.baserbac.common.constant;

/**
 * Redis Key常量
 */
public class RedisKeyConstant {

    /**
     * Token前缀: rbac:token:{userId}
     */
    public static final String TOKEN_PREFIX = "rbac:token:";

    /**
     * RefreshToken前缀: rbac:refresh:{userId}
     */
    public static final String REFRESH_TOKEN_PREFIX = "rbac:refresh:";

    /**
     * 用户权限标识集合前缀: rbac:perms:{userId}
     */
    public static final String PERMISSIONS_PREFIX = "rbac:perms:";

    /**
     * 用户菜单树前缀: rbac:menus:{userId}
     */
    public static final String MENUS_PREFIX = "rbac:menus:";

    /**
     * 验证码前缀: rbac:captcha:{uuid}
     */
    public static final String CAPTCHA_PREFIX = "rbac:captcha:";

    /**
     * 登录锁定前缀: rbac:lock:{username}
     */
    public static final String LOCK_PREFIX = "rbac:lock:";

    /**
     * Token过期时间(秒): 30分钟
     */
    public static final long TOKEN_EXPIRE_SECONDS = 1800;

    /**
     * RefreshToken过期时间(秒): 7天
     */
    public static final long REFRESH_TOKEN_EXPIRE_SECONDS = 604800;

    /**
     * 权限缓存过期时间(秒): 30分钟
     */
    public static final long PERMISSIONS_EXPIRE_SECONDS = 1800;

    /**
     * 菜单缓存过期时间(秒): 30分钟
     */
    public static final long MENUS_EXPIRE_SECONDS = 1800;

    /**
     * 验证码过期时间(秒): 5分钟
     */
    public static final long CAPTCHA_EXPIRE_SECONDS = 300;
}
