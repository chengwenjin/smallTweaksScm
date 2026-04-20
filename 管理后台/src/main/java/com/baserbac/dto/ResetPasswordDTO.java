package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重置密码请求
 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordDTO {

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", example = "123456")
    private String newPassword;
}
