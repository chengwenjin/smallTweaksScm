package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.entity.OaApproval;
import com.baserbac.scm.service.OaApprovalService;
import com.baserbac.scm.vo.OaApprovalVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "OA审批管理")
@RestController
@RequestMapping("/api/scm/oa-approvals")
@RequiredArgsConstructor
public class OaApprovalController {

    private final OaApprovalService approvalService;

    @Operation(summary = "分页查询审批列表")
    @GetMapping
    public R<PageResult<OaApprovalVO>> pageApprovals(
            @RequestParam(required = false) Integer sourceType,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.success(approvalService.pageApprovals(sourceType, approvalStatus, pageNum, pageSize));
    }

    @Operation(summary = "根据ID查询审批")
    @GetMapping("/{id}")
    public R<OaApprovalVO> getApproval(@PathVariable Long id) {
        return R.success(approvalService.getApprovalById(id));
    }

    @OperationLog(module = "OA审批", value = "提交OA审批")
    @Operation(summary = "提交OA审批")
    @PostMapping("/submit")
    public R<OaApproval> submitToOA(
            @RequestParam Integer sourceType,
            @RequestParam Long sourceId,
            @RequestParam(required = false) String sourceNo,
            @RequestParam(required = false) String approvalTitle) {
        String operator = getCurrentOperator();
        OaApproval approval = approvalService.submitToOA(sourceType, sourceId, sourceNo, approvalTitle, operator);
        return R.success(approval);
    }

    @OperationLog(module = "OA审批", value = "处理审批")
    @Operation(summary = "处理审批")
    @PostMapping("/{id}/process")
    public R<Void> processApproval(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String remark) {
        String operator = getCurrentOperator();
        approvalService.processApproval(id, action, remark, operator);
        return R.success();
    }

    @OperationLog(module = "OA审批", value = "撤回审批")
    @Operation(summary = "撤回审批")
    @PostMapping("/{id}/withdraw")
    public R<Void> withdrawApproval(@PathVariable Long id) {
        String operator = getCurrentOperator();
        approvalService.withdrawApproval(id, operator);
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
