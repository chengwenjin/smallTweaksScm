package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 分配权限请求
 */
@Data
@Schema(description = "分配权限请求")
public class AssignPermDTO {

    @NotEmpty(message = "菜单ID列表不能为空")
    @Schema(description = "菜单ID列表", example = "[1, 2, 3]")
    private List<Long> menuIds;

    @Schema(description = "API权限ID列表", example = "[10, 20, 30]")
    private List<Long> apiIds;
}
