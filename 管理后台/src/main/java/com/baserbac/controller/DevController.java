package com.baserbac.controller;

import com.baserbac.common.result.R;
import com.baserbac.common.util.PasswordUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 临时工具控制器 - 用于开发调试
 */
@Tag(name = "临时工具")
@RestController
@RequestMapping("/api/dev")
public class DevController {

    @Operation(summary = "生成BCrypt密码哈希")
    @GetMapping("/generate-password")
    public R<Map<String, String>> generatePassword(@RequestParam String password) {
        String encoded = PasswordUtil.encode(password);
        boolean matches = PasswordUtil.matches(password, encoded);
        
        Map<String, String> result = new HashMap<>();
        result.put("original", password);
        result.put("encoded", encoded);
        result.put("matches", String.valueOf(matches));
        
        return R.success(result);
    }
}
