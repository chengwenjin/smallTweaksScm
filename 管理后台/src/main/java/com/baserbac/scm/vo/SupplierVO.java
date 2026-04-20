package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "供应商信息")
public class SupplierVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "供应商ID")
    private Long id;

    @Schema(description = "供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "供应商类型：1生产型 2贸易型 3服务型")
    private Integer supplierType;

    @Schema(description = "供应商等级：1A级 2AA级 3AAA级")
    private Integer grade;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "状态：0待准入 1已准入 2已冻结 3已淘汰")
    private Integer status;

    @Schema(description = "审核状态：0待审核 1审核中 2审核通过 3审核拒绝")
    private Integer auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "资质列表")
    private List<SupplierQualificationVO> qualifications;
}
