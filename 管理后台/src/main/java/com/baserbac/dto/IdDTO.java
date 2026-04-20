package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * ID请求DTO
 */
@Data
@Schema(description = "ID请求")
public class IdDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键ID", example = "1")
    private Long id;
}
