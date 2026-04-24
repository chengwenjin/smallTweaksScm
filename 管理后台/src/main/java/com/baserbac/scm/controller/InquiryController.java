package com.baserbac.scm.controller;

import com.baserbac.annotation.OperationLog;
import com.baserbac.common.result.R;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.InquiryCreateDTO;
import com.baserbac.scm.dto.InquiryQueryDTO;
import com.baserbac.scm.dto.QuoteSubmitDTO;
import com.baserbac.scm.service.InquiryService;
import com.baserbac.scm.vo.InquirySupplierVO;
import com.baserbac.scm.vo.InquiryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "询价单管理")
@RestController
@RequestMapping("/api/scm/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "分页查询询价单列表")
    @GetMapping
    public R<PageResult<InquiryVO>> pageInquiries(InquiryQueryDTO queryDTO) {
        return R.success(inquiryService.pageInquiries(queryDTO));
    }

    @Operation(summary = "根据ID查询询价单")
    @GetMapping("/{id}")
    public R<InquiryVO> getInquiry(@PathVariable Long id) {
        return R.success(inquiryService.getInquiryById(id));
    }

    @Operation(summary = "获取询价单的供应商报价列表")
    @GetMapping("/{id}/suppliers")
    public R<List<InquirySupplierVO>> getInquirySuppliers(@PathVariable Long id) {
        InquiryVO inquiry = inquiryService.getInquiryById(id);
        return R.success(inquiry.getSuppliers());
    }

    @OperationLog(module = "询价单管理", value = "创建询价单")
    @Operation(summary = "创建询价单（一键询价）")
    @PostMapping
    public R<Void> createInquiry(@Valid @RequestBody InquiryCreateDTO createDTO) {
        inquiryService.createInquiry(createDTO);
        return R.success();
    }

    @OperationLog(module = "询价单管理", value = "发布询价单")
    @Operation(summary = "发布询价单")
    @PutMapping("/{id}/publish")
    public R<Void> publishInquiry(@PathVariable Long id) {
        inquiryService.publishInquiry(id);
        return R.success();
    }

    @OperationLog(module = "询价单管理", value = "提交报价")
    @Operation(summary = "供应商提交报价")
    @PostMapping("/quote")
    public R<Void> submitQuote(@Valid @RequestBody QuoteSubmitDTO submitDTO) {
        inquiryService.submitQuote(submitDTO);
        return R.success();
    }

    @OperationLog(module = "询价单管理", value = "取消询价单")
    @Operation(summary = "取消询价单")
    @PutMapping("/{id}/cancel")
    public R<Void> cancelInquiry(@PathVariable Long id) {
        inquiryService.cancelInquiry(id);
        return R.success();
    }

    @OperationLog(module = "询价单管理", value = "删除询价单")
    @Operation(summary = "删除询价单")
    @DeleteMapping("/{id}")
    public R<Void> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return R.success();
    }
}
