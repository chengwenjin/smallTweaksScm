package com.baserbac.scm.controller;

import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.dto.PageQueryDTO;
import com.baserbac.scm.service.QualificationAlertService;
import com.baserbac.scm.vo.QualificationAlertVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "资质预警管理")
@RestController
@RequestMapping("/api/scm/alerts")
@RequiredArgsConstructor
public class QualificationAlertController {

    private final QualificationAlertService alertService;

    @Operation(summary = "分页查询预警列表")
    @GetMapping
    public R<PageResult<QualificationAlertVO>> pageAlerts(
            PageQueryDTO queryDTO,
            @RequestParam(required = false) Integer isRead) {
        return R.success(alertService.pageAlerts(queryDTO, isRead));
    }

    @Operation(summary = "获取未读预警数量")
    @GetMapping("/unread-count")
    public R<Map<String, Object>> getUnreadCount() {
        long count = alertService.getUnreadCount();
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", count);
        return R.success(result);
    }

    @Operation(summary = "标记预警为已读")
    @PostMapping("/{id}/read")
    public R<Void> markAsRead(@PathVariable Long id) {
        alertService.markAsRead(id);
        return R.success();
    }

    @Operation(summary = "标记所有预警为已读")
    @PostMapping("/read-all")
    public R<Void> markAllAsRead() {
        alertService.markAllAsRead();
        return R.success();
    }
}
