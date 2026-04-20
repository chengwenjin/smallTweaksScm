package com.baserbac.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型枚举
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    /**
     * 目录
     */
    DIRECTORY(1, "目录"),

    /**
     * 菜单
     */
    MENU(2, "菜单"),

    /**
     * 按钮
     */
    BUTTON(3, "按钮");

    private final Integer code;
    private final String desc;

    public static MenuTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MenuTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
