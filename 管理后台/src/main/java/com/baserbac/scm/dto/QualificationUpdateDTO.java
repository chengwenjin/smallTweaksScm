package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 资质更新DTO
 */
@Data
@Schema(description = "资质更新参数")
public class QualificationUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "资质ID")
    private Long id;

    @Schema(description = "资质名称", example = "营业执照")
    private String qualificationName;

    @Schema(description = "证书编号", example = "91110000MA007K2L4A")
    private String certificateNo;

    @Schema(description = "发证日期", example = "2020-01-01")
    private LocalDate issueDate;

    @Schema(description = "有效期至", example = "2025-12-31")
    private LocalDate expiryDate;

    @Schema(description = "是否长期有效：0否 1是", example = "0")
    private Integer isLongTerm;

    @Schema(description = "附件URL列表")
    private List<String> fileUrls;

    @Schema(description = "发证机关", example = "某某市市场监督管理局")
    private String issuingAuthority;

    @Schema(description = "备注")
    private String remark;
}
