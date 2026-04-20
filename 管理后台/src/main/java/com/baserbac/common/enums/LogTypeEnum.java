package com.baserbac.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志类型枚举
 */
@Getter
@AllArgsConstructor
public enum LogTypeEnum {

    /**
     * 登录日志
     */
    LOGIN(1, "登录"),

    /**
     * 操作日志
     */
    OPERATE(2, "操作"),

    /**
     * 异常日志
     */
    EXCEPTION(3, "异常");

    private final Integer code;
    private final String desc;

    public static LogTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LogTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
