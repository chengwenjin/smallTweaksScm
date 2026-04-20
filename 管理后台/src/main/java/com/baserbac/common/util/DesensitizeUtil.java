package com.baserbac.common.util;

import cn.hutool.core.util.StrUtil;

/**
 * 脱敏工具类
 */
public class DesensitizeUtil {

    /**
     * 密码脱敏
     */
    public static String desensitizePassword(String password) {
        if (StrUtil.isBlank(password)) {
            return password;
        }
        return "******";
    }

    /**
     * 手机号脱敏
     */
    public static String desensitizePhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏
     */
    public static String desensitizeEmail(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        if (username.length() <= 2) {
            return "**@" + parts[1];
        }
        return username.substring(0, 2) + "***@" + parts[1];
    }
}
