package com.baserbac.scm.dto;

import com.baserbac.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 资质查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "资质查询参数")
public class QualificationQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "资质类型")
    private String qualificationType;

    @Schema(description = "资质名称")
    private String qualificationName;

    @Schema(description = "审核状态：0待审核 1审核中 2审核通过 3审核拒绝")
    private Integer auditStatus;

    @Schema(description = "预警状态：0正常 1即将到期 2已过期")
    private Integer alertStatus;
}
