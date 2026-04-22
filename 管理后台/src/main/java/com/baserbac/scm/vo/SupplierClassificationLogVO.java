package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分级分类变更记录VO")
public class SupplierClassificationLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "原物资类别")
    private Integer oldMaterialCategory;

    @Schema(description = "新物资类别")
    private Integer newMaterialCategory;

    @Schema(description = "原合作分级")
    private Integer oldCooperationLevel;

    @Schema(description = "新合作分级")
    private Integer newCooperationLevel;

    @Schema(description = "变更原因")
    private String changeReason;

    @Schema(description = "操作人")
    private String createBy;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
