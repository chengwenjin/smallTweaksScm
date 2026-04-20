package com.baserbac.common.util;

import cn.hutool.captcha.LineCaptcha;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证码工具类
 */
public class CaptchaUtil {

    /**
     * 生成图形验证码
     *
     * @return Map包含uuid和Base64图片
     */
    public static Map<String, String> generateCaptcha() {
        // 定义图形验证码的长、宽、验证码字符数、干扰元素个数
        LineCaptcha captcha = cn.hutool.captcha.CaptchaUtil.createLineCaptcha(200, 100, 4, 50);
        
        // 获取验证码文本
        String code = captcha.getCode();
        
        // 获取Base64编码的图片
        String imageBase64 = captcha.getImageBase64Data();
        
        // 生成UUID
        String uuid = java.util.UUID.randomUUID().toString();
        
        Map<String, String> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("captchaImage", "data:image/png;base64," + imageBase64);
        result.put("captchaCode", code); // 实际使用时不应返回给前端
        
        return result;
    }
}
