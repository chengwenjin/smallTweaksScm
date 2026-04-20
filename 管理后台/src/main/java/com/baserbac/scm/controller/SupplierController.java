package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.SupplierCreateDTO;
import com.baserbac.scm.dto.SupplierQueryDTO;
import com.baserbac.scm.dto.SupplierUpdateDTO;
import com.baserbac.scm.service.SupplierService;
import com.baserbac.scm.vo.SupplierVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "供应商管理")
@RestController
@RequestMapping("/api/scm/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "分页查询供应商列表")
    @GetMapping
    public R<PageResult<SupplierVO>> pageSuppliers(SupplierQueryDTO queryDTO) {
        return R.success(supplierService.pageSuppliers(queryDTO));
    }

    @Operation(summary = "根据ID查询供应商")
    @GetMapping("/{id}")
    public R<SupplierVO> getSupplier(@PathVariable Long id) {
        return R.success(supplierService.getSupplierById(id));
    }

    @OperationLog(module = "供应商管理", value = "新增供应商")
    @Operation(summary = "创建供应商")
    @PostMapping
    public R<Void> createSupplier(@Valid @RequestBody SupplierCreateDTO createDTO) {
        supplierService.createSupplier(createDTO);
        return R.success();
    }

    @OperationLog(module = "供应商管理", value = "编辑供应商")
    @Operation(summary = "更新供应商")
    @PutMapping("/{id}")
    public R<Void> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierUpdateDTO updateDTO) {
        updateDTO.setId(id);
        supplierService.updateSupplier(updateDTO);
        return R.success();
    }

    @OperationLog(module = "供应商管理", value = "删除供应商")
    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    public R<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return R.success();
    }
}
