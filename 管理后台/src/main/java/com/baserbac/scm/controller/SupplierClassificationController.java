package com.baserbac.scm.controller;

import com.baserbac.common.result.R;
import com.baserbac.scm.dto.SupplierClassificationDTO;
import com.baserbac.scm.service.SupplierClassificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scm/classification")
@Tag(name = "供应商分级分类", description = "供应商分级分类管理API")
@RequiredArgsConstructor
public class SupplierClassificationController {

    private final SupplierClassificationService classificationService;

    @Operation(summary = "批量设置供应商分级分类")
    @PostMapping("/set")
    public R<Void> setClassification(@Valid @RequestBody SupplierClassificationDTO dto) {
        classificationService.setClassification(dto);
        return R.success();
    }

    @Operation(summary = "分页查询分级分类变更记录")
    @GetMapping("/logs")
    public R<?> getClassificationLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "供应商ID") @RequestParam(required = false) Long supplierId) {
        return R.success(classificationService.pageClassificationLogs(pageNum, pageSize, supplierId));
    }
}
