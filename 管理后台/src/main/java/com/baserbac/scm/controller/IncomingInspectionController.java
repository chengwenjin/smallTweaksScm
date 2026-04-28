package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.IncomingInspectionCreateDTO;
import com.baserbac.scm.dto.IncomingInspectionQueryDTO;
import com.baserbac.scm.service.IncomingInspectionService;
import com.baserbac.scm.vo.IncomingInspectionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "来料质检管理")
@RestController
@RequestMapping("/api/scm/incoming-inspections")
@RequiredArgsConstructor
public class IncomingInspectionController {

    private final IncomingInspectionService inspectionService;

    @Operation(summary = "分页查询来料质检列表")
    @GetMapping
    public R<PageResult<IncomingInspectionVO>> pageInspections(IncomingInspectionQueryDTO queryDTO) {
        return R.success(inspectionService.pageInspections(queryDTO));
    }

    @Operation(summary = "根据ID查询来料质检详情")
    @GetMapping("/{id}")
    public R<IncomingInspectionVO> getInspection(@PathVariable Long id) {
        return R.success(inspectionService.getInspectionById(id));
    }

    @Operation(summary = "获取订单的所有质检记录")
    @GetMapping("/order/{orderId}")
    public R<List<IncomingInspectionVO>> getInspectionsByOrder(@PathVariable Long orderId) {
        return R.success(inspectionService.getInspectionsByOrderId(orderId));
    }

    @OperationLog(module = "来料质检管理", value = "创建质检记录")
    @Operation(summary = "创建来料质检记录")
    @PostMapping
    public R<Void> createInspection(@Valid @RequestBody IncomingInspectionCreateDTO createDTO) {
        inspectionService.createInspection(createDTO);
        return R.success();
    }

    @OperationLog(module = "来料质检管理", value = "提交质检记录")
    @Operation(summary = "提交质检记录")
    @PutMapping("/{id}/submit")
    public R<Void> submitInspection(@PathVariable Long id) {
        inspectionService.submitInspection(id);
        return R.success();
    }

    @OperationLog(module = "来料质检管理", value = "审核质检记录")
    @Operation(summary = "审核质检记录")
    @PutMapping("/{id}/approve")
    public R<Void> approveInspection(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer handlingType = params.get("handlingType") != null 
            ? Integer.parseInt(params.get("handlingType").toString()) 
            : null;
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        inspectionService.approveInspection(id, handlingType, remark);
        return R.success();
    }
}
