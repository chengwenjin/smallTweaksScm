package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "供应商分级分类设置参数")
public class SupplierClassificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "供应商ID不能为空")
    @Schema(description = "供应商ID列表", example = "[1, 2, 3]")
    private List<Long> supplierIds;

    @Schema(description = "物资类别：1原材料 2辅料 3设备", example = "1")
    private Integer materialCategory;

    @Schema(description = "合作分级：1战略 2合格 3潜在", example = "1")
    private Integer cooperationLevel;

    @Schema(description = "变更原因", example = "供应商评估结果调整")
    private String changeReason;
}
