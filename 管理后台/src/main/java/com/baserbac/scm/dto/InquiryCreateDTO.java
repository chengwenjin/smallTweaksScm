package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "询价单创建参数")
public class InquiryCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "询价单名称不能为空")
    @Schema(description = "询价单名称")
    private String inquiryName;

    @Schema(description = "采购需求单ID列表")
    private List<Long> reqIds;

    @Schema(description = "供应商ID列表")
    private List<Long> supplierIds;

    @Schema(description = "报价截止日期")
    private LocalDate deadline;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "询价说明")
    private String description;

    @Schema(description = "备注")
    private String remark;
}
