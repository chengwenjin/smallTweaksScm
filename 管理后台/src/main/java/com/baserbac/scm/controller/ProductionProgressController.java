package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.ProductionProgressQueryDTO;
import com.baserbac.scm.service.ProductionProgressService;
import com.baserbac.scm.vo.ProductionProgressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "生产进度管理")
@RestController
@RequestMapping("/api/scm/production-progress")
@RequiredArgsConstructor
public class ProductionProgressController {

    private final ProductionProgressService progressService;

    @Operation(summary = "分页查询生产进度列表")
    @GetMapping
    public R<PageResult<ProductionProgressVO>> pageProgress(ProductionProgressQueryDTO queryDTO) {
        return R.success(progressService.pageProgress(queryDTO));
    }

    @Operation(summary = "根据ID查询生产进度详情")
    @GetMapping("/{id}")
    public R<ProductionProgressVO> getProgress(@PathVariable Long id) {
        return R.success(progressService.getProgressById(id));
    }

    @Operation(summary = "获取订单的所有生产进度")
    @GetMapping("/order/{orderId}")
    public R<List<ProductionProgressVO>> getProgressByOrder(@PathVariable Long orderId) {
        return R.success(progressService.getProgressByOrderId(orderId));
    }

    @OperationLog(module = "生产进度管理", value = "创建生产进度")
    @Operation(summary = "为订单创建生产进度")
    @PostMapping("/order/{orderId}/create")
    public R<Void> createProgressForOrder(@PathVariable Long orderId) {
        progressService.createProgressForOrder(orderId);
        return R.success();
    }

    @OperationLog(module = "生产进度管理", value = "更新生产进度")
    @Operation(summary = "更新生产进度")
    @PutMapping("/{id}")
    public R<Void> updateProgress(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        BigDecimal completedQuantity = params.get("completedQuantity") != null 
            ? new BigDecimal(params.get("completedQuantity").toString()) 
            : null;
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        progressService.updateProgress(id, completedQuantity, remark);
        return R.success();
    }

    @OperationLog(module = "生产进度管理", value = "更新进度状态")
    @Operation(summary = "更新生产进度状态")
    @PutMapping("/{id}/status")
    public R<Void> updateProgressStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        if (status != null) {
            progressService.updateProgressStatus(id, status);
        }
        return R.success();
    }
}
