package com.baserbac.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.dto.AssignPermDTO;
import com.baserbac.dto.RoleCreateDTO;
import com.baserbac.dto.RoleQueryDTO;
import com.baserbac.dto.RoleStatusDTO;
import com.baserbac.dto.RoleUpdateDTO;
import com.baserbac.service.RoleService;
import com.baserbac.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "分页查询角色列表")
    @GetMapping
    public R<PageResult<RoleVO>> pageRoles(RoleQueryDTO queryDTO) {
        return R.success(roleService.pageRoles(queryDTO));
    }

    @Operation(summary = "查询所有角色")
    @GetMapping("/all")
    public R<List<RoleVO>> listAllRoles() {
        return R.success(roleService.listAllRoles());
    }

    @Operation(summary = "根据ID查询角色")
    @GetMapping("/{id}")
    public R<RoleVO> getRole(@PathVariable Long id) {
        return R.success(roleService.getRoleById(id));
    }

    @OperationLog(module = "角色管理", value = "新增角色")
    @Operation(summary = "创建角色")
    @PostMapping
    public R<Void> createRole(@Valid @RequestBody RoleCreateDTO createDTO) {
        roleService.createRole(createDTO);
        return R.success();
    }

    @OperationLog(module = "角色管理", value = "编辑角色")
    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public R<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO updateDTO) {
        updateDTO.setId(id);
        roleService.updateRole(updateDTO);
        return R.success();
    }

    @OperationLog(module = "角色管理", value = "删除角色")
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public R<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return R.success();
    }

    @OperationLog(module = "角色管理", value = "更新角色状态")
    @Operation(summary = "更新角色状态")
    @PutMapping("/{id}/status")
    public R<Void> updateRoleStatus(@PathVariable Long id, @Valid @RequestBody RoleStatusDTO statusDTO) {
        roleService.updateRoleStatus(id, statusDTO);
        return R.success();
    }

    @OperationLog(module = "角色管理", value = "分配权限")
    @Operation(summary = "分配权限")
    @PutMapping("/{id}/permissions")
    public R<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody AssignPermDTO permDTO) {
        roleService.assignPermissions(id, permDTO);
        return R.success();
    }
}
