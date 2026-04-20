package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.QualificationAuditDTO;
import com.baserbac.scm.dto.QualificationCreateDTO;
import com.baserbac.scm.dto.QualificationQueryDTO;
import com.baserbac.scm.dto.QualificationUpdateDTO;
import com.baserbac.scm.service.SupplierQualificationService;
import com.baserbac.scm.vo.SupplierQualificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "资质审核管理")
@RestController
@RequestMapping("/api/scm/qualifications")
@RequiredArgsConstructor
public class SupplierQualificationController {

    private final SupplierQualificationService qualificationService;

    @Operation(summary = "分页查询资质列表")
    @GetMapping
    public R<PageResult<SupplierQualificationVO>> pageQualifications(QualificationQueryDTO queryDTO) {
        return R.success(qualificationService.pageQualifications(queryDTO));
    }

    @Operation(summary = "根据ID查询资质")
    @GetMapping("/{id}")
    public R<SupplierQualificationVO> getQualification(@PathVariable Long id) {
        return R.success(qualificationService.getQualificationById(id));
    }

    @OperationLog(module = "资质审核管理", value = "新增资质")
    @Operation(summary = "创建资质")
    @PostMapping
    public R<Void> createQualification(@Valid @RequestBody QualificationCreateDTO createDTO) {
        qualificationService.createQualification(createDTO);
        return R.success();
    }

    @OperationLog(module = "资质审核管理", value = "编辑资质")
    @Operation(summary = "更新资质")
    @PutMapping("/{id}")
    public R<Void> updateQualification(@PathVariable Long id, @Valid @RequestBody QualificationUpdateDTO updateDTO) {
        updateDTO.setId(id);
        qualificationService.updateQualification(updateDTO);
        return R.success();
    }

    @OperationLog(module = "资质审核管理", value = "删除资质")
    @Operation(summary = "删除资质")
    @DeleteMapping("/{id}")
    public R<Void> deleteQualification(@PathVariable Long id) {
        qualificationService.deleteQualification(id);
        return R.success();
    }

    @OperationLog(module = "资质审核管理", value = "审核资质")
    @Operation(summary = "审核资质")
    @PostMapping("/{id}/audit")
    public R<Void> auditQualification(@PathVariable Long id, @Valid @RequestBody QualificationAuditDTO auditDTO) {
        qualificationService.auditQualification(id, auditDTO);
        return R.success();
    }
}
