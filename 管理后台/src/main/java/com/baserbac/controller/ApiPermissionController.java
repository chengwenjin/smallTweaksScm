package com.baserbac.controller;

import com.baserbac.common.result.PageResult;
import com.baserbac.common.result.R;
import com.baserbac.entity.SysApiPermission;
import com.baserbac.service.ApiPermissionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API权限管理控制器
 */
@Tag(name = "API权限管理")
@RestController
@RequestMapping("/api/system/api-permissions")
@RequiredArgsConstructor
public class ApiPermissionController {

    private final ApiPermissionService apiPermissionService;

    @Operation(summary = "分页查询API权限")
    @GetMapping
    public R<PageResult<SysApiPermission>> pageApiPermissions(
            @RequestParam(required = false) String apiName,
            @RequestParam(required = false) String permissionKey,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<SysApiPermission> page = apiPermissionService.pageApiPermissions(apiName, permissionKey, status, pageNum, pageSize);
        return R.success(PageResult.of(page.getTotal(), page.getRecords(), (long) page.getCurrent(), (long) page.getSize()));
    }

    @Operation(summary = "根据ID查询API权限")
    @GetMapping("/{id}")
    public R<SysApiPermission> getApiPermission(@PathVariable Long id) {
        return R.success(apiPermissionService.getApiPermissionById(id));
    }

    @Operation(summary = "创建API权限")
    @PostMapping
    public R<Void> createApiPermission(@RequestBody SysApiPermission permission) {
        apiPermissionService.createApiPermission(permission);
        return R.success();
    }

    @Operation(summary = "更新API权限")
    @PutMapping("/{id}")
    public R<Void> updateApiPermission(@PathVariable Long id, @RequestBody SysApiPermission permission) {
        permission.setId(id);
        apiPermissionService.updateApiPermission(permission);
        return R.success();
    }

    @Operation(summary = "删除API权限")
    @DeleteMapping("/{id}")
    public R<Void> deleteApiPermission(@PathVariable Long id) {
        apiPermissionService.deleteApiPermission(id);
        return R.success();
    }

    @Operation(summary = "获取所有启用的API权限")
    @GetMapping("/enabled")
    public R<List<SysApiPermission>> getAllEnabledPermissions() {
        return R.success(apiPermissionService.getAllEnabledPermissions());
    }
}
