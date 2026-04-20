package com.baserbac.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.dto.AssignRoleDTO;
import com.baserbac.dto.IdDTO;
import com.baserbac.dto.ResetPasswordDTO;
import com.baserbac.dto.UserCreateDTO;
import com.baserbac.dto.UserQueryDTO;
import com.baserbac.dto.UserStatusDTO;
import com.baserbac.dto.UserUpdateDTO;
import com.baserbac.service.UserService;
import com.baserbac.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public R<PageResult<UserVO>> pageUsers(UserQueryDTO queryDTO) {
        return R.success(userService.pageUsers(queryDTO));
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public R<UserVO> getUser(@PathVariable Long id) {
        return R.success(userService.getUserById(id));
    }

    @OperationLog(module = "用户管理", value = "新增用户")
    @Operation(summary = "创建用户")
    @PostMapping
    public R<Void> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        userService.createUser(createDTO);
        return R.success();
    }

    @OperationLog(module = "用户管理", value = "编辑用户")
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public R<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO updateDTO) {
        updateDTO.setId(id);
        userService.updateUser(updateDTO);
        return R.success();
    }

    @OperationLog(module = "用户管理", value = "删除用户")
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.success();
    }

    @OperationLog(module = "用户管理", value = "更新用户状态")
    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public R<Void> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusDTO statusDTO) {
        userService.updateUserStatus(id, statusDTO);
        return R.success();
    }

    @OperationLog(module = "用户管理", value = "重置密码")
    @Operation(summary = "重置密码")
    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO passwordDTO) {
        userService.resetPassword(id, passwordDTO);
        return R.success();
    }

    @OperationLog(module = "用户管理", value = "分配角色")
    @Operation(summary = "分配角色")
    @PutMapping("/{id}/roles")
    public R<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRoleDTO roleDTO) {
        userService.assignRoles(id, roleDTO);
        return R.success();
    }
}
