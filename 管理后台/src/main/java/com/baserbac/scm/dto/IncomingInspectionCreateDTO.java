package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "来料质检创建参数")
public class IncomingInspectionCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(description = "发货ID")
    private Long shipmentId;

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "检验类型：1抽样检验 2全检")
    private Integer inspectionType = 1;

    @NotBlank(message = "检验员不能为空")
    @Schema(description = "检验员", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inspector;

    @NotBlank(message = "物料编码不能为空")
    @Schema(description = "物料编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialCode;

    @NotBlank(message = "物料名称不能为空")
    @Schema(description = "物料名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialName;

    @Schema(description = "物料规格")
    private String materialSpec;

    @Schema(description = "物料单位")
    private String materialUnit;

    @Schema(description = "批号")
    private String batchNo;

    @Schema(description = "生产日期")
    private LocalDate productionDate;

    @Schema(description = "有效期至")
    private LocalDate expiryDate;

    @NotNull(message = "总数量不能为空")
    @Schema(description = "总数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalQuantity;

    @Schema(description = "检验数量")
    private BigDecimal inspectionQuantity;

    @Schema(description = "抽样数量")
    private BigDecimal sampleQuantity;

    @Schema(description = "合格数量")
    private BigDecimal passQuantity;

    @Schema(description = "不合格数量")
    private BigDecimal failQuantity;

    @Schema(description = "检验结果：1合格 2让步接收 3退货")
    private Integer inspectionResult;

    @Schema(description = "检验项目")
    private String inspectionItems;

    @Schema(description = "检验数据")
    private String inspectionData;

    @Schema(description = "缺陷描述")
    private String defectDescription;

    @Schema(description = "处理建议")
    private String handlingSuggestion;

    @Schema(description = "处理方式：1入库 2让步接收 3退货 4待复检")
    private Integer handlingType;

    @Schema(description = "备注")
    private String remark;
}
