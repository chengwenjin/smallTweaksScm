package com.baserbac.controller;

import com.baserbac.common.result.PageResult;
import com.baserbac.common.result.R;
import com.baserbac.entity.SysLoginLog;
import com.baserbac.service.LoginLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志控制器
 */
@Tag(name = "登录日志管理")
@RestController
@RequestMapping("/api/system/login-logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "分页查询登录日志")
    @GetMapping
    public R<PageResult<SysLoginLog>> pageLoginLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer loginStatus,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<SysLoginLog> page = loginLogService.pageLoginLogs(
            username, loginStatus, startTime, endTime, pageNum, pageSize);
        return R.success(PageResult.of(page.getTotal(), page.getRecords(), 
            (long) page.getCurrent(), (long) page.getSize()));
    }

    @Operation(summary = "根据ID查询登录日志")
    @GetMapping("/{id}")
    public R<SysLoginLog> getLoginLog(@PathVariable Long id) {
        return R.success(loginLogService.getLoginLogById(id));
    }

    @Operation(summary = "删除登录日志")
    @DeleteMapping("/{id}")
    public R<Void> deleteLoginLog(@PathVariable Long id) {
        loginLogService.deleteLoginLog(id);
        return R.success();
    }

    @Operation(summary = "批量删除登录日志")
    @DeleteMapping("/batch")
    public R<Void> batchDeleteLoginLogs(@RequestParam String ids) {
        loginLogService.batchDeleteLoginLogs(ids);
        return R.success();
    }

    @Operation(summary = "清空所有登录日志")
    @DeleteMapping("/clear")
    public R<Void> clearAllLoginLogs() {
        loginLogService.clearAllLoginLogs();
        return R.success();
    }
}
