package com.baserbac.scm.dto;

import com.baserbac.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 供应商查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "供应商查询参数")
public class SupplierQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "供应商类型：1生产型 2贸易型 3服务型")
    private Integer supplierType;

    @Schema(description = "供应商等级：1A级 2AA级 3AAA级")
    private Integer grade;

    @Schema(description = "物资类别：1原材料 2辅料 3设备")
    private Integer materialCategory;

    @Schema(description = "合作分级：1战略 2合格 3潜在")
    private Integer cooperationLevel;

    @Schema(description = "状态：0待准入 1已准入 2已冻结 3已淘汰")
    private Integer status;

    @Schema(description = "审核状态：0待审核 1审核中 2审核通过 3审核拒绝")
    private Integer auditStatus;
}
