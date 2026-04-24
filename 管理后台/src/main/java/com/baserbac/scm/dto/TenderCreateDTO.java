package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "招标单创建参数")
public class TenderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "招标单名称不能为空")
    @Schema(description = "招标单名称")
    private String tenderName;

    @Schema(description = "招标类型：1公开招标 2邀请招标")
    private Integer tenderType;

    @Schema(description = "采购需求单ID列表")
    private List<Long> reqIds;

    @Schema(description = "预算金额")
    private BigDecimal estimatedBudget;

    @Schema(description = "招标描述")
    private String description;

    @Schema(description = "发布日期")
    private LocalDate publishDate;

    @Schema(description = "投标截止日期")
    private LocalDate bidDeadline;

    @Schema(description = "开标日期")
    private LocalDate openDate;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "招标地址")
    private String tenderAddress;

    @Schema(description = "招标文件URL列表")
    private String tenderDocs;

    @Schema(description = "备注")
    private String remark;
}
