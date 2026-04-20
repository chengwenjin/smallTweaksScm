package com.baserbac.controller;

import com.baserbac.common.result.PageResult;
import com.baserbac.common.result.R;
import com.baserbac.entity.SysOperationLog;
import com.baserbac.service.OperationLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/api/system/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    public R<PageResult<SysOperationLog>> pageOperationLogs(
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer logType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<SysOperationLog> page = operationLogService.pageOperationLogs(
            operatorName, module, logType, startTime, endTime, pageNum, pageSize);
        return R.success(PageResult.of(page.getTotal(), page.getRecords(), 
            (long) page.getCurrent(), (long) page.getSize()));
    }

    @Operation(summary = "根据ID查询操作日志")
    @GetMapping("/{id}")
    public R<SysOperationLog> getOperationLog(@PathVariable Long id) {
        return R.success(operationLogService.getOperationLogById(id));
    }

    @Operation(summary = "删除操作日志")
    @DeleteMapping("/{id}")
    public R<Void> deleteOperationLog(@PathVariable Long id) {
        operationLogService.deleteOperationLog(id);
        return R.success();
    }

    @Operation(summary = "批量删除操作日志")
    @DeleteMapping("/batch")
    public R<Void> batchDeleteOperationLogs(@RequestParam String ids) {
        operationLogService.batchDeleteOperationLogs(ids);
        return R.success();
    }

    @Operation(summary = "清空所有操作日志")
    @DeleteMapping("/clear")
    public R<Void> clearAllOperationLogs() {
        operationLogService.clearAllOperationLogs();
        return R.success();
    }
}
