package com.baserbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询DTO
 */
@Data
@Schema(description = "分页查询参数")
public class PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "排序字段")
    private String orderBy;

    @Schema(description = "排序方向：asc/desc", example = "desc")
    private String orderDirection = "desc";
}
