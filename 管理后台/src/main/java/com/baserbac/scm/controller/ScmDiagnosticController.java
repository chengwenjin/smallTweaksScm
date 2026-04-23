package com.baserbac.scm.controller;

import com.baserbac.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "SCM诊断管理")
@RestController
@RequestMapping("/api/scm/diagnostic")
@RequiredArgsConstructor
public class ScmDiagnosticController {

    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "检查数据库原始数据（跳过逻辑删除）")
    @GetMapping("/check-data")
    public R<Map<String, Object>> checkData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                "SELECT id, supplier_name, is_deleted, create_time FROM scm_supplier ORDER BY id");
            result.put("suppliers", suppliers);
            
            List<Map<String, Object>> qualifications = jdbcTemplate.queryForList(
                "SELECT id, supplier_id, qualification_name, audit_status, alert_status, is_deleted FROM scm_supplier_qualification ORDER BY id");
            result.put("qualifications", qualifications);
            
            List<Map<String, Object>> alerts = jdbcTemplate.queryForList(
                "SELECT id, supplier_id, qualification_id, alert_title, is_read, is_deleted FROM scm_qualification_alert ORDER BY id");
            result.put("alerts", alerts);
            
            log.info("检查数据结果: 供应商={}条, 资质={}条, 预警={}条", 
                suppliers.size(), qualifications.size(), alerts.size());
            
            return R.success(result);
        } catch (Exception e) {
            log.error("检查数据失败", e);
            return R.error(500, "检查数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "修复is_deleted字段（设置为0）")
    @PostMapping("/fix-is-deleted")
    public R<Map<String, Object>> fixIsDeleted() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int supplierCount = jdbcTemplate.update(
                "UPDATE scm_supplier SET is_deleted = 0 WHERE is_deleted IS NULL OR is_deleted != 0");
            result.put("suppliers_updated", supplierCount);
            
            int qualificationCount = jdbcTemplate.update(
                "UPDATE scm_supplier_qualification SET is_deleted = 0 WHERE is_deleted IS NULL OR is_deleted != 0");
            result.put("qualifications_updated", qualificationCount);
            
            int alertCount = jdbcTemplate.update(
                "UPDATE scm_qualification_alert SET is_deleted = 0 WHERE is_deleted IS NULL OR is_deleted != 0");
            result.put("alerts_updated", alertCount);
            
            int logCount = jdbcTemplate.update(
                "UPDATE scm_supplier_classification_log SET is_deleted = 0 WHERE is_deleted IS NULL OR is_deleted != 0");
            result.put("classification_logs_updated", logCount);
            
            log.info("修复is_deleted完成: 供应商={}, 资质={}, 预警={}, 分级记录={}",
                supplierCount, qualificationCount, alertCount, logCount);
            
            return R.success(result);
        } catch (Exception e) {
            log.error("修复is_deleted失败", e);
            return R.error(500, "修复失败: " + e.getMessage());
        }
    }

    @Operation(summary = "修复表及字段注释乱码")
    @PostMapping("/fix-comments")
    public R<Map<String, Object>> fixComments() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            jdbcTemplate.execute("SET NAMES utf8mb4");
            jdbcTemplate.execute("SET CHARACTER SET utf8mb4");
            
            List<String> commentSqls = List.of(
                "ALTER TABLE scm_supplier COMMENT = '供应商基础信息表'",
                "ALTER TABLE scm_supplier_qualification COMMENT = '供应商资质审核表'",
                "ALTER TABLE scm_qualification_alert COMMENT = '资质预警记录表'",
                "ALTER TABLE scm_supplier_classification_log COMMENT = '供应商分级分类变更记录表'",
                
                "ALTER TABLE scm_supplier MODIFY COLUMN supplier_code VARCHAR(50) COMMENT '供应商编码'",
                "ALTER TABLE scm_supplier MODIFY COLUMN supplier_name VARCHAR(100) COMMENT '供应商名称'",
                "ALTER TABLE scm_supplier MODIFY COLUMN supplier_type TINYINT COMMENT '供应商类型：1生产型 2贸易型 3服务型'",
                "ALTER TABLE scm_supplier MODIFY COLUMN grade TINYINT COMMENT '供应商等级：1A级 2AA级 3AAA级'",
                "ALTER TABLE scm_supplier MODIFY COLUMN material_category TINYINT COMMENT '物资类别：1原材料 2辅料 3设备'",
                "ALTER TABLE scm_supplier MODIFY COLUMN cooperation_level TINYINT COMMENT '合作分级：1战略 2合格 3潜在'",
                "ALTER TABLE scm_supplier MODIFY COLUMN contact_person VARCHAR(50) COMMENT '联系人'",
                "ALTER TABLE scm_supplier MODIFY COLUMN contact_phone VARCHAR(20) COMMENT '联系电话'",
                "ALTER TABLE scm_supplier MODIFY COLUMN contact_email VARCHAR(100) COMMENT '联系邮箱'",
                "ALTER TABLE scm_supplier MODIFY COLUMN address VARCHAR(255) COMMENT '详细地址'",
                "ALTER TABLE scm_supplier MODIFY COLUMN status TINYINT COMMENT '状态：0待准入 1已准入 2已冻结 3已淘汰'",
                "ALTER TABLE scm_supplier MODIFY COLUMN audit_status TINYINT COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝'",
                "ALTER TABLE scm_supplier MODIFY COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'",
                
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN supplier_id BIGINT COMMENT '供应商ID'",
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN qualification_type VARCHAR(50) COMMENT '资质类型：BUSINESS_LICENSE营业执照, TAX_REGISTRATION税务登记证等'",
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN qualification_name VARCHAR(100) COMMENT '资质名称'",
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN certificate_no VARCHAR(100) COMMENT '证书编号'",
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN issuing_authority VARCHAR(100) COMMENT '发证机关'",
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN issue_date DATE COMMENT '发证日期'",
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN expiry_date DATE COMMENT '有效期至'",
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN is_long_term TINYINT COMMENT '是否长期有效：0否 1是'",
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN audit_status TINYINT COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝'",
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN alert_status TINYINT COMMENT '预警状态：0正常 1即将到期 2已过期'",
                "ALTER TABLE scm_supplier_qualification MODIFY COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'",
                
                "ALTER TABLE scm_qualification_alert MODIFY COLUMN qualification_id BIGINT COMMENT '资质ID'",
                "ALTER TABLE scm_qualification_alert MODIFY COLUMN supplier_id BIGINT COMMENT '供应商ID'",
                "ALTER TABLE scm_qualification_alert MODIFY COLUMN alert_type TINYINT COMMENT '预警类型：1即将到期 2已过期'",
                "ALTER TABLE scm_qualification_alert MODIFY COLUMN alert_title VARCHAR(100) COMMENT '预警标题'",
                "ALTER TABLE scm_qualification_alert MODIFY COLUMN is_read TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读'",
                "ALTER TABLE scm_qualification_alert MODIFY COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'",
                
                "ALTER TABLE scm_supplier_classification_log MODIFY COLUMN supplier_id BIGINT COMMENT '供应商ID'",
                "ALTER TABLE scm_supplier_classification_log MODIFY COLUMN old_material_category TINYINT COMMENT '原物资类别'",
                "ALTER TABLE scm_supplier_classification_log MODIFY COLUMN new_material_category TINYINT COMMENT '新物资类别'",
                "ALTER TABLE scm_supplier_classification_log MODIFY COLUMN old_cooperation_level TINYINT COMMENT '原合作分级'",
                "ALTER TABLE scm_supplier_classification_log MODIFY COLUMN new_cooperation_level TINYINT COMMENT '新合作分级'",
                "ALTER TABLE scm_supplier_classification_log MODIFY COLUMN change_reason VARCHAR(500) COMMENT '变更原因'",
                "ALTER TABLE scm_supplier_classification_log MODIFY COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'"
            );
            
            for (String sql : commentSqls) {
                try {
                    jdbcTemplate.execute(sql);
                } catch (Exception e) {
                    log.warn("执行SQL失败: {}", sql, e);
                }
            }
            
            result.put("message", "表及字段注释修复完成");
            log.info("表及字段注释修复完成");
            
            return R.success(result);
        } catch (Exception e) {
            log.error("修复注释失败", e);
            return R.error(500, "修复失败: " + e.getMessage());
        }
    }

    @Operation(summary = "一键修复所有问题")
    @PostMapping("/fix-all")
    public R<Map<String, Object>> fixAll() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("fix_is_deleted", fixIsDeleted().getData());
            result.put("fix_comments", fixComments().getData());
            return R.success(result);
        } catch (Exception e) {
            log.error("一键修复失败", e);
            return R.error(500, "修复失败: " + e.getMessage());
        }
    }
}
