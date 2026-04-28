package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.DeliveryAppointmentCreateDTO;
import com.baserbac.scm.dto.DeliveryAppointmentQueryDTO;
import com.baserbac.scm.service.DeliveryAppointmentService;
import com.baserbac.scm.vo.DeliveryAppointmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "送货预约管理")
@RestController
@RequestMapping("/api/scm/delivery-appointments")
@RequiredArgsConstructor
public class DeliveryAppointmentController {

    private final DeliveryAppointmentService appointmentService;

    @Operation(summary = "分页查询送货预约列表")
    @GetMapping
    public R<PageResult<DeliveryAppointmentVO>> pageAppointments(DeliveryAppointmentQueryDTO queryDTO) {
        return R.success(appointmentService.pageAppointments(queryDTO));
    }

    @Operation(summary = "根据ID查询送货预约详情")
    @GetMapping("/{id}")
    public R<DeliveryAppointmentVO> getAppointment(@PathVariable Long id) {
        return R.success(appointmentService.getAppointmentById(id));
    }

    @OperationLog(module = "送货预约管理", value = "创建预约")
    @Operation(summary = "创建送货预约")
    @PostMapping
    public R<Void> createAppointment(@Valid @RequestBody DeliveryAppointmentCreateDTO createDTO) {
        appointmentService.createAppointment(createDTO);
        return R.success();
    }

    @OperationLog(module = "送货预约管理", value = "确认预约")
    @Operation(summary = "确认送货预约")
    @PutMapping("/{id}/confirm")
    public R<Void> confirmAppointment(@PathVariable Long id) {
        appointmentService.confirmAppointment(id);
        return R.success();
    }

    @OperationLog(module = "送货预约管理", value = "签到")
    @Operation(summary = "送货签到")
    @PutMapping("/{id}/check-in")
    public R<Void> checkInAppointment(@PathVariable Long id) {
        appointmentService.checkInAppointment(id);
        return R.success();
    }

    @OperationLog(module = "送货预约管理", value = "完成预约")
    @Operation(summary = "完成送货预约")
    @PutMapping("/{id}/complete")
    public R<Void> completeAppointment(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String warehouseOperator = params.get("warehouseOperator");
        appointmentService.completeAppointment(id, warehouseOperator);
        return R.success();
    }

    @OperationLog(module = "送货预约管理", value = "取消预约")
    @Operation(summary = "取消送货预约")
    @PutMapping("/{id}/cancel")
    public R<Void> cancelAppointment(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        appointmentService.cancelAppointment(id, reason);
        return R.success();
    }
}
