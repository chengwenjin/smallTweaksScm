package com.baserbac.controller;

import com.baserbac.common.result.R;
import com.baserbac.common.util.SecurityUtil;
import com.baserbac.dto.LoginDTO;
import com.baserbac.service.AuthService;
import com.baserbac.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public R<Map<String, String>> getCaptcha() {
        return R.success(authService.generateCaptcha());
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return R.success(authService.login(loginDTO));
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public R<LoginVO> refreshToken(@RequestParam String refreshToken) {
        return R.success(authService.refreshToken(refreshToken));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId != null) {
            authService.logout(userId);
        }
        return R.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public R<Map<String, Object>> getUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return R.error(401, "未登录");
        }
        return R.success(authService.getUserInfo(userId));
    }
}
