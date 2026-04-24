package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.PriceComparisonDTO;
import com.baserbac.scm.service.PriceComparisonService;
import com.baserbac.scm.vo.PriceComparisonVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "比价管理")
@RestController
@RequestMapping("/api/scm/comparisons")
@RequiredArgsConstructor
public class PriceComparisonController {

    private final PriceComparisonService comparisonService;

    @Operation(summary = "分页查询比价单列表")
    @GetMapping
    public R<PageResult<PriceComparisonVO>> pageComparisons(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long inquiryId,
            @RequestParam(required = false) Integer status) {
        return R.success(comparisonService.pageComparisons(pageNum, pageSize, inquiryId, status));
    }

    @Operation(summary = "根据ID查询比价单")
    @GetMapping("/{id}")
    public R<PriceComparisonVO> getComparison(@PathVariable Long id) {
        return R.success(comparisonService.getComparisonById(id));
    }

    @OperationLog(module = "比价管理", value = "创建比价单")
    @Operation(summary = "创建比价单（智能比价）")
    @PostMapping
    public R<PriceComparisonVO> createComparison(@RequestBody(required = false) Map<String, Object> params,
            @RequestParam(required = false) Long inquiryId) {
        Long targetInquiryId = inquiryId;
        if (targetInquiryId == null && params != null && params.containsKey("inquiryId")) {
            Object idObj = params.get("inquiryId");
            if (idObj instanceof Number) {
                targetInquiryId = ((Number) idObj).longValue();
            } else if (idObj instanceof String) {
                targetInquiryId = Long.parseLong((String) idObj);
            }
        }
        if (targetInquiryId == null) {
            return R.error(400, "缺少必要参数inquiryId");
        }
        return R.success(comparisonService.createComparison(targetInquiryId));
    }

    @OperationLog(module = "比价管理", value = "开始比价")
    @Operation(summary = "开始智能比价")
    @PostMapping("/{id}/start")
    public R<PriceComparisonVO> startComparison(@PathVariable Long id) {
        PriceComparisonVO vo = comparisonService.getComparisonById(id);
        if (vo == null) {
            return R.error(404, "比价单不存在");
        }
        return R.success(vo);
    }

    @OperationLog(module = "比价管理", value = "确认推荐方案")
    @Operation(summary = "确认推荐采购方案")
    @PostMapping("/{id}/confirm")
    public R<Void> confirmRecommendation(@PathVariable Long id, @Valid @RequestBody PriceComparisonDTO dto) {
        dto.setId(id);
        comparisonService.confirmRecommendation(dto);
        return R.success();
    }
}
