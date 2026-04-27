package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.DemandSummaryQueryDTO;
import com.baserbac.scm.entity.DemandSummary;
import com.baserbac.scm.service.DemandSummaryService;
import com.baserbac.scm.vo.DemandSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "需求汇总管理")
@RestController
@RequestMapping("/api/scm/demand-summaries")
@RequiredArgsConstructor
public class DemandSummaryController {

    private final DemandSummaryService summaryService;

    @Operation(summary = "分页查询需求汇总列表")
    @GetMapping
    public R<PageResult<DemandSummaryVO>> pageSummaries(DemandSummaryQueryDTO queryDTO) {
        return R.success(summaryService.pageSummaries(queryDTO));
    }

    @Operation(summary = "根据ID查询需求汇总")
    @GetMapping("/{id}")
    public R<DemandSummaryVO> getSummary(@PathVariable Long id) {
        return R.success(summaryService.getSummaryById(id));
    }

    @OperationLog(module = "需求汇总", value = "生成需求汇总")
    @Operation(summary = "生成需求汇总")
    @PostMapping("/generate")
    public R<DemandSummary> generateSummary(
            @RequestParam(required = false, defaultValue = "1") Integer periodType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String materialCategory) {
        String operator = getCurrentOperator();
        DemandSummary summary = summaryService.generateSummary(periodType, year, month, materialCategory, operator);
        return R.success(summary);
    }

    @OperationLog(module = "需求汇总", value = "确认需求汇总")
    @Operation(summary = "确认需求汇总")
    @PostMapping("/{id}/confirm")
    public R<Void> confirmSummary(@PathVariable Long id) {
        String operator = getCurrentOperator();
        summaryService.confirmSummary(id, operator);
        return R.success();
    }

    private String getCurrentOperator() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                return authentication.getName();
            }
        } catch (Exception e) {
        }
        return "admin";
    }
}
