package com.baserbac.scm.dto;

import com.baserbac.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "送货预约查询参数")
public class DeliveryAppointmentQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "预约单号")
    private String appointmentNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "预约日期")
    private LocalDate appointmentDate;

    @Schema(description = "仓库ID")
    private Integer warehouseId;

    @Schema(description = "状态：0待确认 1已确认 2已签到 3已完成 4已取消")
    private Integer status;
}
