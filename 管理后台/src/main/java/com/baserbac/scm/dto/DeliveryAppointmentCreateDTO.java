package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(description = "送货预约创建参数")
public class DeliveryAppointmentCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @NotNull(message = "预约日期不能为空")
    @Schema(description = "预约日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate appointmentDate;

    @NotNull(message = "预约开始时间不能为空")
    @Schema(description = "预约开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime appointmentTimeFrom;

    @NotNull(message = "预约结束时间不能为空")
    @Schema(description = "预约结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime appointmentTimeTo;

    @Schema(description = "送货地址")
    private String deliveryAddress;

    @Schema(description = "仓库ID")
    private Integer warehouseId;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "停靠点编号")
    private Integer dockNo;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "车牌号")
    private String vehicleNo;

    @Schema(description = "司机姓名")
    private String driverName;

    @Schema(description = "司机电话")
    private String driverPhone;

    @Schema(description = "备注")
    private String remark;
}
