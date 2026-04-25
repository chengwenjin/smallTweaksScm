package com.baserbac.scm.dto;

import com.baserbac.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "供应商KPI查询参数")
public class SupplierKpiQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "周期类型：1月度 2季度 3年度")
    private Integer periodType;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "季度")
    private Integer quarter;

    @Schema(description = "等级：1A级 2AA级 3AAA级")
    private Integer grade;

    @Schema(description = "状态")
    private Integer status;
}
