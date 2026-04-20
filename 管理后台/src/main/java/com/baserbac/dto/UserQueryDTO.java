package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询DTO
 */
@Data
@Schema(description = "用户查询参数")
public class UserQueryDTO extends PageQueryDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名（模糊查询）")
    private String username;

    @Schema(description = "真实姓名（模糊查询）")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
