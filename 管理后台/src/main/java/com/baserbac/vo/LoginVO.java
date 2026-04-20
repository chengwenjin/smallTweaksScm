package com.baserbac.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "访问Token")
    private String accessToken;

    @Schema(description = "刷新Token")
    private String refreshToken;

    @Schema(description = "Token类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "过期时间（秒）", example = "1800")
    private Long expiresIn;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;
}
