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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "来料质检信息")
public class IncomingInspectionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "检验单号")
    private String inspectionNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "发货记录ID")
    private Long shipmentId;

    @Schema(description = "发货单号")
    private String shipmentNo;

    @Schema(description = "送货预约ID")
    private Long appointmentId;

    @Schema(description = "预约单号")
    private String appointmentNo;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "检验类型：1抽样检验 2全检")
    private Integer inspectionType;

    @Schema(description = "检验类型名称")
    private String inspectionTypeName;

    @Schema(description = "检验日期")
    private LocalDate inspectionDate;

    @Schema(description = "检验员")
    private String inspector;

    @Schema(description = "检验员名称")
    private String inspectorName;

    @Schema(description = "物料编码")
    private String materialCode;

    @Schema(description = "物料名称")
    private String materialName;

    @Schema(description = "规格型号")
    private String materialSpec;

    @Schema(description = "单位")
    private String materialUnit;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "生产日期")
    private LocalDate productionDate;

    @Schema(description = "保质期/有效期")
    private LocalDate expiryDate;

    @Schema(description = "总数量")
    private BigDecimal totalQuantity;

    @Schema(description = "检验数量")
    private BigDecimal inspectionQuantity;

    @Schema(description = "抽样数量")
    private BigDecimal sampleQuantity;

    @Schema(description = "合格数量")
    private BigDecimal passQuantity;

    @Schema(description = "不合格数量")
    private BigDecimal failQuantity;

    @Schema(description = "检验结果：0待检验 1合格 2让步接收 3退货 4待复检")
    private Integer inspectionResult;

    @Schema(description = "检验结果名称")
    private String inspectionResultName;

    @Schema(description = "合格率")
    private String passRate;

    @Schema(description = "检验项目（JSON）")
    private String inspectionItems;

    @Schema(description = "检验数据（JSON）")
    private String inspectionData;

    @Schema(description = "不合格描述")
    private String defectDescription;

    @Schema(description = "处理建议")
    private String handlingSuggestion;

    @Schema(description = "处理类型：1入库 2让步接收 3退货 4待复检")
    private Integer handlingType;

    @Schema(description = "处理类型名称")
    private String handlingTypeName;

    @Schema(description = "入库单ID")
    private Long warehouseReceiptId;

    @Schema(description = "入库单号")
    private String warehouseReceiptNo;

    @Schema(description = "是否关联入库")
    private Boolean linkedToReceipt;

    @Schema(description = "检验结论")
    private String conclusion;

    @Schema(description = "不合格原因")
    private String unqualifiedReason;

    @Schema(description = "处理意见")
    private String handlingOpinion;

    @Schema(description = "审批人")
    private String approverName;

    @Schema(description = "审批日期")
    private LocalDate approvalDate;

    @Schema(description = "状态：0草稿 1已提交 2已检验 3已审批")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
