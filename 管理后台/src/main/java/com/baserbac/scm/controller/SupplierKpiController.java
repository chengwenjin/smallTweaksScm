package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.SupplierKpiQueryDTO;
import com.baserbac.scm.service.SupplierKpiService;
import com.baserbac.scm.vo.SupplierKpiVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "供应商KPI管理")
@RestController
@RequestMapping("/api/scm/kpis")
@RequiredArgsConstructor
public class SupplierKpiController {

    private final SupplierKpiService kpiService;

    @Operation(summary = "分页查询KPI列表")
    @GetMapping
    public R<PageResult<SupplierKpiVO>> pageKpis(SupplierKpiQueryDTO queryDTO) {
        return R.success(kpiService.pageKpis(queryDTO));
    }

    @Operation(summary = "根据ID查询KPI")
    @GetMapping("/{id}")
    public R<SupplierKpiVO> getKpi(@PathVariable Long id) {
        return R.success(kpiService.getKpiById(id));
    }

    @OperationLog(module = "供应商KPI管理", value = "计算KPI")
    @Operation(summary = "手动触发KPI计算")
    @PostMapping("/calculate")
    public R<Void> calculateKpis(
            @RequestParam(required = false, defaultValue = "2") Integer periodType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer month) {
        int currentYear = java.time.LocalDate.now().getYear();
        int targetYear = year != null ? year : currentYear;
        int targetQuarter = quarter != null ? quarter : 1;
        int targetMonth = month != null ? month : 1;
        kpiService.calculateKpis(periodType, targetYear, targetQuarter, targetMonth);
        return R.success();
    }
}
