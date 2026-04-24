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
@Schema(description = "招标单VO")
public class TenderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "招标单ID")
    private Long id;

    @Schema(description = "招标单编号")
    private String tenderNo;

    @Schema(description = "招标单名称")
    private String tenderName;

    @Schema(description = "招标类型：1公开招标 2邀请招标")
    private Integer tenderType;

    @Schema(description = "招标描述")
    private String description;

    @Schema(description = "采购需求单ID列表")
    private String reqIds;

    @Schema(description = "预算金额")
    private BigDecimal estimatedBudget;

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

    @Schema(description = "招标文件URL列表")
    private String tenderDocs;

    @Schema(description = "招标地址")
    private String tenderAddress;

    @Schema(description = "状态：0待发布 1招标中 2投标中 3已开标 4评标中 5已定标 6已取消")
    private Integer status;

    @Schema(description = "中标供应商ID")
    private Long winSupplierId;

    @Schema(description = "中标供应商名称")
    private String winSupplierName;

    @Schema(description = "中标价格")
    private BigDecimal winPrice;

    @Schema(description = "中标原因")
    private String winReason;

    @Schema(description = "中标日期")
    private LocalDate winDate;

    @Schema(description = "投标列表")
    private List<TenderBidVO> bids;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
