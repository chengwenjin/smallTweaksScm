package com.baserbac.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.dto.MenuCreateDTO;
import com.baserbac.dto.MenuQueryDTO;
import com.baserbac.dto.MenuUpdateDTO;
import com.baserbac.service.MenuService;
import com.baserbac.vo.MenuTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "获取菜单树")
    @GetMapping("/tree")
    public R<List<MenuTreeVO>> getMenuTree(MenuQueryDTO queryDTO) {
        return R.success(menuService.getMenuTree(queryDTO));
    }

    @Operation(summary = "根据ID查询菜单")
    @GetMapping("/{id}")
    public R<MenuTreeVO> getMenu(@PathVariable Long id) {
        return R.success(menuService.getMenuById(id));
    }

    @OperationLog(module = "菜单管理", value = "新增菜单")
    @Operation(summary = "创建菜单")
    @PostMapping
    public R<Void> createMenu(@RequestBody MenuCreateDTO createDTO) {
        menuService.createMenu(createDTO);
        return R.success();
    }

    @OperationLog(module = "菜单管理", value = "编辑菜单")
    @Operation(summary = "更新菜单")
    @PutMapping
    public R<Void> updateMenu(@RequestBody MenuUpdateDTO updateDTO) {
        menuService.updateMenu(updateDTO);
        return R.success();
    }

    @OperationLog(module = "菜单管理", value = "删除菜单")
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public R<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.success();
    }
}
