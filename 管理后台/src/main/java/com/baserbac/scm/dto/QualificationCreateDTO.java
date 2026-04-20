package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 资质创建DTO
 */
@Data
@Schema(description = "资质创建参数")
public class QualificationCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "供应商ID不能为空")
    @Schema(description = "供应商ID", example = "1")
    private Long supplierId;

    @NotBlank(message = "资质类型不能为空")
    @Schema(description = "资质类型：BUSINESS_LICENSE营业执照, TAX_REGISTRATION税务登记证, ORG_CODE组织机构代码证, PRODUCT_CERT产品认证, QUALITY_CERT质量认证, OTHER其他", example = "BUSINESS_LICENSE")
    private String qualificationType;

    @NotBlank(message = "资质名称不能为空")
    @Schema(description = "资质名称", example = "营业执照")
    private String qualificationName;

    @Schema(description = "证书编号", example = "91110000MA007K2L4A")
    private String certificateNo;

    @Schema(description = "发证日期", example = "2020-01-01")
    private LocalDate issueDate;

    @Schema(description = "有效期至", example = "2025-12-31")
    private LocalDate expiryDate;

    @Schema(description = "是否长期有效：0否 1是", example = "0")
    private Integer isLongTerm = 0;

    @Schema(description = "附件URL列表")
    private List<String> fileUrls;

    @Schema(description = "发证机关", example = "某某市市场监督管理局")
    private String issuingAuthority;

    @Schema(description = "备注")
    private String remark;
}
