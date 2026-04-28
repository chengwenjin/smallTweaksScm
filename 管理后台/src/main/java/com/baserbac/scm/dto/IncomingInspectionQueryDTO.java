package com.baserbac.scm.dto;

import com.baserbac.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "来料质检查询参数")
public class IncomingInspectionQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "检验单号")
    private String inspectionNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "发货ID")
    private Long shipmentId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "物料编码")
    private String materialCode;

    @Schema(description = "物料名称")
    private String materialName;

    @Schema(description = "检验类型：1抽样检验 2全检")
    private Integer inspectionType;

    @Schema(description = "检验结果：0待检验 1合格 2让步接收 3退货 4待复检")
    private Integer inspectionResult;

    @Schema(description = "状态：0草稿 1已提交 2已审核")
    private Integer status;

    @Schema(description = "检验日期开始")
    private LocalDate inspectionDateStart;

    @Schema(description = "检验日期结束")
    private LocalDate inspectionDateEnd;
}
