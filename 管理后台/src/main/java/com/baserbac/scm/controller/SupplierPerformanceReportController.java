package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.PerformanceReportQueryDTO;
import com.baserbac.scm.service.SupplierPerformanceReportService;
import com.baserbac.scm.vo.PerformanceReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "绩效报告管理")
@RestController
@RequestMapping("/api/scm/reports")
@RequiredArgsConstructor
public class SupplierPerformanceReportController {

    private final SupplierPerformanceReportService reportService;

    @Operation(summary = "分页查询绩效报告列表")
    @GetMapping
    public R<PageResult<PerformanceReportVO>> pageReports(PerformanceReportQueryDTO queryDTO) {
        return R.success(reportService.pageReports(queryDTO));
    }

    @Operation(summary = "根据ID查询绩效报告")
    @GetMapping("/{id}")
    public R<PerformanceReportVO> getReport(@PathVariable Long id) {
        return R.success(reportService.getReportById(id));
    }

    @OperationLog(module = "绩效报告管理", value = "生成报告")
    @Operation(summary = "手动触发生成绩效报告")
    @PostMapping("/generate")
    public R<Void> generateReports(
            @RequestParam(required = false, defaultValue = "1") Integer reportType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter) {
        int currentYear = java.time.LocalDate.now().getYear();
        int targetYear = year != null ? year : currentYear;
        int targetQuarter = quarter != null ? quarter : 1;
        reportService.generateReports(reportType, targetYear, targetQuarter);
        return R.success();
    }
}
