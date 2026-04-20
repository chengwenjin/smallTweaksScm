package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新DTO
 */
@Data
@Schema(description = "用户更新参数")
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;

    @Schema(description = "角色ID列表")
    private java.util.List<Long> roleIds;

    @Schema(description = "备注")
    private String remark;
}
