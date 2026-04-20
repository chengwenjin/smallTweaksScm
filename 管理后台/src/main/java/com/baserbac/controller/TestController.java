package com.baserbac.controller;

import com.baserbac.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@Tag(name = "测试接口")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("message", "Base RBAC System is running!");
        return R.success(data);
    }

    @Operation(summary = "获取系统信息")
    @GetMapping("/info")
    public R<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Base RBAC");
        info.put("version", "1.0.0");
        info.put("description", "RBAC Permission Management System");
        return R.success(info);
    }
}
