package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.ShipmentQueryDTO;
import com.baserbac.scm.service.ShipmentService;
import com.baserbac.scm.vo.LogisticsTrackVO;
import com.baserbac.scm.vo.ShipmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "发货管理")
@RestController
@RequestMapping("/api/scm/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @Operation(summary = "分页查询发货列表")
    @GetMapping
    public R<PageResult<ShipmentVO>> pageShipments(ShipmentQueryDTO queryDTO) {
        return R.success(shipmentService.pageShipments(queryDTO));
    }

    @Operation(summary = "根据ID查询发货详情")
    @GetMapping("/{id}")
    public R<ShipmentVO> getShipment(@PathVariable Long id) {
        return R.success(shipmentService.getShipmentById(id));
    }

    @Operation(summary = "获取物流轨迹")
    @GetMapping("/{id}/tracks")
    public R<List<LogisticsTrackVO>> getLogisticsTracks(@PathVariable Long id) {
        return R.success(shipmentService.getLogisticsTracks(id));
    }

    @OperationLog(module = "发货管理", value = "创建发货单")
    @Operation(summary = "为订单创建发货单")
    @PostMapping("/order/{orderId}/create")
    public R<ShipmentVO> createShipment(@PathVariable Long orderId) {
        ShipmentVO shipment = null;
        com.baserbac.scm.entity.Shipment entity = shipmentService.createShipment(orderId);
        return R.success();
    }

    @OperationLog(module = "发货管理", value = "更新发货状态")
    @Operation(summary = "更新发货状态并记录物流轨迹")
    @PutMapping("/{id}/status")
    public R<Void> updateShipmentStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        String location = params.get("location") != null ? params.get("location").toString() : null;
        String description = params.get("description") != null ? params.get("description").toString() : null;
        
        if (status != null) {
            shipmentService.updateShipmentStatus(id, status, location, description);
        }
        return R.success();
    }
}
