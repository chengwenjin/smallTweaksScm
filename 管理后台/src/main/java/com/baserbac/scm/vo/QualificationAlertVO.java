package com.baserbac.scm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资质预警VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资质预警信息")
public class QualificationAlertVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "预警ID")
    private Long id;

    @Schema(description = "资质ID")
    private Long qualificationId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "资质名称")
    private String qualificationName;

    @Schema(description = "预警类型：1即将到期 2已过期")
    private Integer alertType;

    @Schema(description = "预警标题")
    private String alertTitle;

    @Schema(description = "预警内容")
    private String alertContent;

    @Schema(description = "预警日期")
    private LocalDate alertDate;

    @Schema(description = "到期前天数")
    private Integer daysBeforeExpiry;

    @Schema(description = "是否已读：0未读 1已读")
    private Integer isRead;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
