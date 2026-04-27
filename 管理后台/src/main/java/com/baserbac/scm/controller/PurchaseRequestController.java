package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.PurchaseRequestCreateDTO;
import com.baserbac.scm.dto.PurchaseRequestQueryDTO;
import com.baserbac.scm.entity.PurchaseRequest;
import com.baserbac.scm.service.PurchaseRequestService;
import com.baserbac.scm.vo.PurchaseRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "采购申请管理")
@RestController
@RequestMapping("/api/scm/purchase-requests")
@RequiredArgsConstructor
public class PurchaseRequestController {

    private final PurchaseRequestService requestService;

    @Operation(summary = "分页查询采购申请列表")
    @GetMapping
    public R<PageResult<PurchaseRequestVO>> pageRequests(PurchaseRequestQueryDTO queryDTO) {
        return R.success(requestService.pageRequests(queryDTO));
    }

    @Operation(summary = "根据ID查询采购申请")
    @GetMapping("/{id}")
    public R<PurchaseRequestVO> getRequest(@PathVariable Long id) {
        return R.success(requestService.getRequestById(id));
    }

    @OperationLog(module = "采购申请", value = "新增采购申请")
    @Operation(summary = "创建采购申请")
    @PostMapping
    public R<PurchaseRequest> createRequest(@Valid @RequestBody PurchaseRequestCreateDTO createDTO) {
        String operator = getCurrentOperator();
        PurchaseRequest request = requestService.createRequest(createDTO, operator);
        return R.success(request);
    }

    @OperationLog(module = "采购申请", value = "提交采购申请")
    @Operation(summary = "提交采购申请")
    @PostMapping("/{id}/submit")
    public R<Void> submitRequest(@PathVariable Long id) {
        String operator = getCurrentOperator();
        requestService.submitRequest(id, operator);
        return R.success();
    }

    @OperationLog(module = "采购申请", value = "更新采购申请状态")
    @Operation(summary = "更新采购申请状态")
    @PutMapping("/{id}/status")
    public R<Void> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) String remark) {
        String operator = getCurrentOperator();
        requestService.updateRequestStatus(id, status, approvalStatus, remark, operator);
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
