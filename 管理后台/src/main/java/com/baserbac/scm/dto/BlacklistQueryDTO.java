package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "黑名单查询参数")
public class BlacklistQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "黑名单类型：1严重违约 2质量问题 3欺诈行为 4其他")
    private Integer blacklistType;

    @Schema(description = "状态：1在黑名单 2已移除")
    private Integer status;
}
