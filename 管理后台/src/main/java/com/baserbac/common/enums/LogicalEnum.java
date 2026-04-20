package com.baserbac.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 逻辑关系枚举（用于多权限校验）
 */
@Getter
@AllArgsConstructor
public enum LogicalEnum {

    /**
     * 且（所有权限都需要满足）
     */
    AND("AND"),

    /**
     * 或（满足任一权限即可）
     */
    OR("OR");

    private final String value;
}
