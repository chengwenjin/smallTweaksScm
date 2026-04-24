package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.PurchaseRequirementCreateDTO;
import com.baserbac.scm.dto.PurchaseRequirementQueryDTO;
import com.baserbac.scm.dto.PurchaseRequirementUpdateDTO;
import com.baserbac.scm.service.PurchaseRequirementService;
import com.baserbac.scm.vo.PurchaseRequirementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "采购需求单管理")
@RestController
@RequestMapping("/api/scm/requirements")
@RequiredArgsConstructor
public class PurchaseRequirementController {

    private final PurchaseRequirementService requirementService;

    @Operation(summary = "分页查询采购需求单列表")
    @GetMapping
    public R<PageResult<PurchaseRequirementVO>> pageRequirements(PurchaseRequirementQueryDTO queryDTO) {
        return R.success(requirementService.pageRequirements(queryDTO));
    }

    @Operation(summary = "根据ID查询采购需求单")
    @GetMapping("/{id}")
    public R<PurchaseRequirementVO> getRequirement(@PathVariable Long id) {
        return R.success(requirementService.getRequirementById(id));
    }

    @OperationLog(module = "采购需求单管理", value = "新增采购需求单")
    @Operation(summary = "创建采购需求单")
    @PostMapping
    public R<Void> createRequirement(@Valid @RequestBody PurchaseRequirementCreateDTO createDTO) {
        requirementService.createRequirement(createDTO);
        return R.success();
    }

    @OperationLog(module = "采购需求单管理", value = "编辑采购需求单")
    @Operation(summary = "更新采购需求单")
    @PutMapping("/{id}")
    public R<Void> updateRequirement(@PathVariable Long id, @Valid @RequestBody PurchaseRequirementUpdateDTO updateDTO) {
        updateDTO.setId(id);
        requirementService.updateRequirement(updateDTO);
        return R.success();
    }

    @OperationLog(module = "采购需求单管理", value = "删除采购需求单")
    @Operation(summary = "删除采购需求单")
    @DeleteMapping("/{id}")
    public R<Void> deleteRequirement(@PathVariable Long id) {
        requirementService.deleteRequirement(id);
        return R.success();
    }
}
