package com.baserbac.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 日志描述
     */
    String value() default "";
    
    /**
     * 操作模块
     */
    String module() default "";
}
