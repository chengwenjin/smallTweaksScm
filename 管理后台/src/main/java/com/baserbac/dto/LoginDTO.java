package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求DTO
 */
@Data
@Schema(description = "登录请求")
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "admin123")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String captcha;

    @NotBlank(message = "验证码Key不能为空")
    @Schema(description = "验证码Key")
    private String captchaKey;
}
