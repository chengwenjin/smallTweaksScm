package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 供应商更新DTO
 */
@Data
@Schema(description = "供应商更新参数")
public class SupplierUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "供应商ID")
    private Long id;

    @Schema(description = "供应商名称", example = "某某供应商有限公司")
    private String supplierName;

    @Schema(description = "供应商类型：1生产型 2贸易型 3服务型", example = "1")
    private Integer supplierType;

    @Schema(description = "供应商等级：1A级 2AA级 3AAA级", example = "1")
    private Integer grade;

    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "联系邮箱", example = "contact@example.com")
    private String contactEmail;

    @Schema(description = "详细地址", example = "某某市某某区某某路123号")
    private String address;

    @Schema(description = "状态：0待准入 1已准入 2已冻结 3已淘汰", example = "0")
    private Integer status;

    @Schema(description = "审核状态：0待审核 1审核中 2审核通过 3审核拒绝", example = "0")
    private Integer auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "备注")
    private String remark;
}
