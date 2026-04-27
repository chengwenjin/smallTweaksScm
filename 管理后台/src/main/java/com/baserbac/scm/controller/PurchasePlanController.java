package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.PurchasePlanQueryDTO;
import com.baserbac.scm.entity.PurchasePlan;
import com.baserbac.scm.service.PurchasePlanService;
import com.baserbac.scm.vo.PurchasePlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "采购计划管理")
@RestController
@RequestMapping("/api/scm/purchase-plans")
@RequiredArgsConstructor
public class PurchasePlanController {

    private final PurchasePlanService planService;

    @Operation(summary = "分页查询采购计划列表")
    @GetMapping
    public R<PageResult<PurchasePlanVO>> pagePlans(PurchasePlanQueryDTO queryDTO) {
        return R.success(planService.pagePlans(queryDTO));
    }

    @Operation(summary = "根据ID查询采购计划")
    @GetMapping("/{id}")
    public R<PurchasePlanVO> getPlan(@PathVariable Long id) {
        return R.success(planService.getPlanById(id));
    }

    @OperationLog(module = "智能补货", value = "生成补货计划")
    @Operation(summary = "生成补货计划")
    @PostMapping("/generate-replenishment")
    public R<PurchasePlan> generateReplenishmentPlan(
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        String operator = getCurrentOperator();
        PurchasePlan plan = planService.generateReplenishmentPlan(sourceType, year, month, operator);
        return R.success(plan);
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
