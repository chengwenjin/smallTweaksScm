package com.baserbac.annotation;

import com.baserbac.common.enums.LogicalEnum;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于Controller方法上，标识该方法需要的权限标识
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 权限标识
     * 例如：sys:user:add, sys:role:edit
     */
    String value();
    
    /**
     * 多权限时的逻辑关系（预留）
     * AND：需要拥有所有权限
     * OR：拥有任一权限即可
     */
    LogicalEnum logical() default LogicalEnum.AND;
}
