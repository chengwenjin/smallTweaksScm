package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.OrderCancelDTO;
import com.baserbac.scm.dto.OrderChangeDTO;
import com.baserbac.scm.dto.OrderConfirmDTO;
import com.baserbac.scm.dto.PurchaseOrderCreateDTO;
import com.baserbac.scm.dto.PurchaseOrderQueryDTO;
import com.baserbac.scm.dto.PurchaseOrderUpdateDTO;
import com.baserbac.scm.service.PurchaseOrderService;
import com.baserbac.scm.vo.OrderChangeVO;
import com.baserbac.scm.vo.PurchaseOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/scm/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService orderService;

    @Operation(summary = "分页查询订单列表")
    @GetMapping
    public R<PageResult<PurchaseOrderVO>> pageOrders(PurchaseOrderQueryDTO queryDTO) {
        return R.success(orderService.pageOrders(queryDTO));
    }

    @Operation(summary = "根据ID查询订单详情")
    @GetMapping("/{id}")
    public R<PurchaseOrderVO> getOrder(@PathVariable Long id) {
        return R.success(orderService.getOrderById(id));
    }

    @Operation(summary = "获取订单变更记录")
    @GetMapping("/{id}/changes")
    public R<List<OrderChangeVO>> getOrderChanges(@PathVariable Long id) {
        return R.success(orderService.getOrderChanges(id));
    }

    @OperationLog(module = "订单管理", value = "创建订单")
    @Operation(summary = "创建采购订单")
    @PostMapping
    public R<Void> createOrder(@Valid @RequestBody PurchaseOrderCreateDTO createDTO) {
        orderService.createOrder(createDTO);
        return R.success();
    }

    @OperationLog(module = "订单管理", value = "更新订单")
    @Operation(summary = "更新采购订单")
    @PutMapping("/{id}")
    public R<Void> updateOrder(@PathVariable Long id, @Valid @RequestBody PurchaseOrderUpdateDTO updateDTO) {
        updateDTO.setId(id);
        orderService.updateOrder(updateDTO);
        return R.success();
    }

    @OperationLog(module = "订单管理", value = "下发订单")
    @Operation(summary = "下发订单给供应商")
    @PutMapping("/{id}/issue")
    public R<Void> issueOrder(@PathVariable Long id) {
        orderService.issueOrder(id);
        return R.success();
    }

    @OperationLog(module = "订单管理", value = "确认订单")
    @Operation(summary = "供应商确认接单")
    @PostMapping("/confirm")
    public R<Void> confirmOrder(@Valid @RequestBody OrderConfirmDTO confirmDTO) {
        orderService.confirmOrder(confirmDTO);
        return R.success();
    }

    @OperationLog(module = "订单管理", value = "变更订单")
    @Operation(summary = "申请订单变更")
    @PostMapping("/change")
    public R<Void> changeOrder(@Valid @RequestBody OrderChangeDTO changeDTO) {
        orderService.changeOrder(changeDTO);
        return R.success();
    }

    @OperationLog(module = "订单管理", value = "取消订单")
    @Operation(summary = "取消订单")
    @PostMapping("/cancel")
    public R<Void> cancelOrder(@Valid @RequestBody OrderCancelDTO cancelDTO) {
        orderService.cancelOrder(cancelDTO);
        return R.success();
    }
}
