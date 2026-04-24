package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "询价单VO")
public class InquiryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "询价单ID")
    private Long id;

    @Schema(description = "询价单编号")
    private String inquiryNo;

    @Schema(description = "询价单名称")
    private String inquiryName;

    @Schema(description = "采购需求单ID列表")
    private String reqIds;

    @Schema(description = "状态：0待发布 1已发布 2报价中 3报价结束 4已比价 5已取消")
    private Integer status;

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

    @Schema(description = "供应商报价列表")
    private List<InquirySupplierVO> suppliers;

    @Schema(description = "采购需求单列表")
    private List<PurchaseRequirementVO> requirements;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
