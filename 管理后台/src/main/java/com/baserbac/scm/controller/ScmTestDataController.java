package com.baserbac.scm.controller;

import com.baserbac.common.result.R;
import com.baserbac.scm.service.SupplierService;
import com.baserbac.scm.service.SupplierQualificationService;
import com.baserbac.scm.service.QualificationAlertService;
import com.baserbac.scm.service.SupplierClassificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SCM测试数据控制器
 * 用于添加测试数据
 */
@Tag(name = "SCM测试数据")
@RestController
@RequestMapping("/api/scm/test-data")
@RequiredArgsConstructor
public class ScmTestDataController {

    private final SupplierService supplierService;
    private final SupplierQualificationService qualificationService;
    private final QualificationAlertService alertService;
    private final SupplierClassificationService classificationService;

    @Operation(summary = "添加测试数据")
    @PostMapping("/add")
    public R<Void> addTestData() {
        try {
            // 添加供应商测试数据
            supplierService.addTestData();
            
            // 添加资质测试数据
            qualificationService.addTestData();
            
            // 添加预警测试数据
            alertService.addTestData();
            
            // 添加分级分类测试数据
            classificationService.addTestData();
            
            return R.success();
        } catch (Exception e) {
            return R.error(500, "添加测试数据失败: " + e.getMessage());
        }
    }
}