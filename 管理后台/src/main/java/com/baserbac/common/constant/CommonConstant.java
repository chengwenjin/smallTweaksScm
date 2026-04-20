package com.baserbac.common.constant;

/**
 * 通用常量
 */
public class CommonConstant {

    /**
     * 超级管理员角色ID
     */
    public static final Long SUPER_ADMIN_ROLE_ID = 1L;

    /**
     * 超级管理员用户ID
     */
    public static final Long SUPER_ADMIN_USER_ID = 1L;

    /**
     * 成功状态码
     */
    public static final Integer SUCCESS_CODE = 200;

    /**
     * 启用状态
     */
    public static final Integer STATUS_ENABLE = 1;

    /**
     * 禁用状态
     */
    public static final Integer STATUS_DISABLE = 0;

    /**
     * 未删除
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * 已删除
     */
    public static final Integer DELETED = 1;

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 请求头中的Token键
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 默认页码
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页条数
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页条数
     */
    public static final Integer MAX_PAGE_SIZE = 100;
}
