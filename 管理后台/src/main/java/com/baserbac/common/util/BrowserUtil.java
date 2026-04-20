package com.baserbac.common.util;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;

/**
 * 浏览器信息工具类
 */
public class BrowserUtil {

    /**
     * 解析User-Agent获取浏览器信息
     */
    public static String getBrowser(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        UserAgent ua = UserAgentUtil.parse(userAgent);
        return ua.getBrowser().getName();
    }

    /**
     * 解析User-Agent获取操作系统信息
     */
    public static String getOs(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        UserAgent ua = UserAgentUtil.parse(userAgent);
        return ua.getOs().getName();
    }
}
