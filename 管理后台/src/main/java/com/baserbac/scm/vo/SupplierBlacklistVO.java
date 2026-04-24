package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "黑名单信息")
public class SupplierBlacklistVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "黑名单类型：1严重违约 2质量问题 3欺诈行为 4其他")
    private Integer blacklistType;

    @Schema(description = "黑名单类型名称")
    private String blacklistTypeName;

    @Schema(description = "列入黑名单原因")
    private String blacklistReason;

    @Schema(description = "列入日期")
    private LocalDate blacklistDate;

    @Schema(description = "是否永久：0否 1是")
    private Integer isPermanent;

    @Schema(description = "到期日期")
    private LocalDate expireDate;

    @Schema(description = "状态：1在黑名单 2已移除")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "移除原因")
    private String removeReason;

    @Schema(description = "移除日期")
    private LocalDate removeDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
