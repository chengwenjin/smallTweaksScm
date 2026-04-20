package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商资质VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "供应商资质信息")
public class SupplierQualificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "资质ID")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "资质类型：BUSINESS_LICENSE营业执照, TAX_REGISTRATION税务登记证, ORG_CODE组织机构代码证, PRODUCT_CERT产品认证, QUALITY_CERT质量认证, OTHER其他")
    private String qualificationType;

    @Schema(description = "资质名称")
    private String qualificationName;

    @Schema(description = "证书编号")
    private String certificateNo;

    @Schema(description = "发证日期")
    private LocalDate issueDate;

    @Schema(description = "有效期至")
    private LocalDate expiryDate;

    @Schema(description = "是否长期有效：0否 1是")
    private Integer isLongTerm;

    @Schema(description = "附件URL列表")
    private List<String> fileUrls;

    @Schema(description = "发证机关")
    private String issuingAuthority;

    @Schema(description = "审核状态：0待审核 1审核中 2审核通过 3审核拒绝")
    private Integer auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "审核人")
    private String auditBy;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "预警状态：0正常 1即将到期 2已过期")
    private Integer alertStatus;

    @Schema(description = "距离到期天数")
    private Integer daysToExpiry;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
