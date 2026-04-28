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
@Schema(description = "发货记录信息")
public class ShipmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "发货单号")
    private String shipmentNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "发货类型：1首次发货 2分批发货")
    private Integer shipmentType;

    @Schema(description = "发货类型名称")
    private String shipmentTypeName;

    @Schema(description = "发货日期")
    private LocalDate shipmentDate;

    @Schema(description = "预计到达日期")
    private LocalDate estimatedArrivalDate;

    @Schema(description = "实际到达日期")
    private LocalDate actualArrivalDate;

    @Schema(description = "物料种类数量")
    private Integer itemCount;

    @Schema(description = "总数量")
    private BigDecimal totalQuantity;

    @Schema(description = "总重量")
    private BigDecimal totalWeight;

    @Schema(description = "运输方式")
    private String shippingMethod;

    @Schema(description = "承运人/物流公司")
    private String carrier;

    @Schema(description = "物流公司名称")
    private String logisticsCompany;

    @Schema(description = "运单号")
    private String waybillNo;

    @Schema(description = "发货地")
    private String departurePlace;

    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "状态：0待发货 1已发货 2运输中 3已送达 4已签收")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "物流轨迹列表")
    private List<LogisticsTrackVO> tracks;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
