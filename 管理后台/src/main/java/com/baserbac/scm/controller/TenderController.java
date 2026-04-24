package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.TenderAwardDTO;
import com.baserbac.scm.dto.TenderBidDTO;
import com.baserbac.scm.dto.TenderCreateDTO;
import com.baserbac.scm.dto.TenderQueryDTO;
import com.baserbac.scm.service.TenderService;
import com.baserbac.scm.vo.TenderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "招投标管理")
@RestController
@RequestMapping("/api/scm/tenders")
@RequiredArgsConstructor
public class TenderController {

    private final TenderService tenderService;

    @Operation(summary = "分页查询招标单列表")
    @GetMapping
    public R<PageResult<TenderVO>> pageTenders(TenderQueryDTO queryDTO) {
        return R.success(tenderService.pageTenders(queryDTO));
    }

    @Operation(summary = "根据ID查询招标单")
    @GetMapping("/{id}")
    public R<TenderVO> getTender(@PathVariable Long id) {
        return R.success(tenderService.getTenderById(id));
    }

    @OperationLog(module = "招投标管理", value = "创建招标单")
    @Operation(summary = "创建招标单")
    @PostMapping
    public R<Void> createTender(@Valid @RequestBody TenderCreateDTO createDTO) {
        tenderService.createTender(createDTO);
        return R.success();
    }

    @OperationLog(module = "招投标管理", value = "更新招标单")
    @Operation(summary = "更新招标单")
    @PutMapping("/{id}")
    public R<Void> updateTender(@PathVariable Long id, @Valid @RequestBody TenderCreateDTO updateDTO) {
        tenderService.updateTender(id, updateDTO);
        return R.success();
    }

    @OperationLog(module = "招投标管理", value = "发布招标单")
    @Operation(summary = "发布招标单")
    @PostMapping("/{id}/publish")
    public R<Void> publishTender(@PathVariable Long id) {
        tenderService.publishTender(id);
        return R.success();
    }

    @OperationLog(module = "招投标管理", value = "提交投标")
    @Operation(summary = "供应商提交投标")
    @PostMapping("/bid")
    public R<Void> submitBid(@Valid @RequestBody TenderBidDTO bidDTO) {
        tenderService.submitBid(bidDTO);
        return R.success();
    }

    @OperationLog(module = "招投标管理", value = "开标")
    @Operation(summary = "开标")
    @PostMapping("/{id}/open")
    public R<Void> openTender(@PathVariable Long id) {
        tenderService.openTender(id);
        return R.success();
    }

    @OperationLog(module = "招投标管理", value = "定标")
    @Operation(summary = "定标（确定中标供应商）")
    @PostMapping("/award")
    public R<Void> awardTender(@Valid @RequestBody TenderAwardDTO awardDTO) {
        tenderService.awardTender(awardDTO);
        return R.success();
    }

    @OperationLog(module = "招投标管理", value = "取消招标")
    @Operation(summary = "取消招标")
    @PostMapping("/{id}/cancel")
    public R<Void> cancelTender(@PathVariable Long id) {
        tenderService.cancelTender(id);
        return R.success();
    }

    @OperationLog(module = "招投标管理", value = "删除招标单")
    @Operation(summary = "删除招标单")
    @DeleteMapping("/{id}")
    public R<Void> deleteTender(@PathVariable Long id) {
        tenderService.deleteTender(id);
        return R.success();
    }
}
