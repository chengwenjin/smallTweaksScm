package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.BlacklistCreateDTO;
import com.baserbac.scm.dto.BlacklistQueryDTO;
import com.baserbac.scm.dto.BlacklistRemoveDTO;
import com.baserbac.scm.service.SupplierBlacklistService;
import com.baserbac.scm.vo.SupplierBlacklistVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "黑名单管理")
@RestController
@RequestMapping("/api/scm/blacklists")
@RequiredArgsConstructor
public class SupplierBlacklistController {

    private final SupplierBlacklistService blacklistService;

    @Operation(summary = "分页查询黑名单列表")
    @GetMapping
    public R<PageResult<SupplierBlacklistVO>> pageBlacklists(BlacklistQueryDTO queryDTO) {
        return R.success(blacklistService.pageBlacklists(queryDTO));
    }

    @Operation(summary = "根据ID查询黑名单详情")
    @GetMapping("/{id}")
    public R<SupplierBlacklistVO> getBlacklist(@PathVariable Long id) {
        return R.success(blacklistService.getBlacklistById(id));
    }

    @Operation(summary = "检查供应商是否在黑名单中")
    @GetMapping("/check/{supplierId}")
    public R<Boolean> checkSupplierBlacklisted(@PathVariable Long supplierId) {
        return R.success(blacklistService.isSupplierBlacklisted(supplierId));
    }

    @OperationLog(module = "黑名单管理", value = "列入黑名单")
    @Operation(summary = "将供应商列入黑名单")
    @PostMapping
    public R<Void> addToBlacklist(@Valid @RequestBody BlacklistCreateDTO createDTO) {
        blacklistService.addToBlacklist(createDTO);
        return R.success();
    }

    @OperationLog(module = "黑名单管理", value = "移除黑名单")
    @Operation(summary = "将供应商从黑名单移除")
    @PutMapping("/{id}/remove")
    public R<Void> removeFromBlacklist(@PathVariable Long id, @Valid @RequestBody BlacklistRemoveDTO removeDTO) {
        blacklistService.removeFromBlacklist(id, removeDTO);
        return R.success();
    }
}
