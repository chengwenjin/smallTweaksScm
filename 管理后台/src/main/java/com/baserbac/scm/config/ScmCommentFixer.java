package com.baserbac.scm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Order(99)
@RequiredArgsConstructor
public class ScmCommentFixer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("  检查并修复SCM表中文注释");
        log.info("========================================");

        try {
            jdbcTemplate.execute("SET NAMES utf8mb4");
            jdbcTemplate.execute("SET CHARACTER SET utf8mb4");
            
            log.info("\n========== 修复前的表注释 ==========");
            showTableComments();
            
            log.info("\n========== 开始执行修复 ==========");
            
            List<String> sqls = getFixSqls();
            int success = 0;
            int fail = 0;
            List<String> failed = new ArrayList<>();

            for (String sql : sqls) {
                try {
                    jdbcTemplate.execute(sql);
                    success++;
                    String display = sql.length() > 60 ? sql.substring(0, 60) + "..." : sql;
                    log.info("OK: {}", display);
                } catch (Exception e) {
                    fail++;
                    failed.add(sql);
                    String display = sql.length() > 60 ? sql.substring(0, 60) + "..." : sql;
                    log.warn("FAIL: {} - {}", display, e.getMessage());
                }
            }

            log.info("\n========== 执行结果 ==========");
            log.info("成功: {} 条", success);
            log.info("失败: {} 条", fail);

            if (!failed.isEmpty()) {
                log.info("\n失败的SQL:");
                for (String s : failed) {
                    log.info("  - {}", s.substring(0, Math.min(80, s.length())));
                }
            }

            log.info("\n========== 修复后的表注释 ==========");
            showTableComments();

            log.info("\n========== 修复后的字段注释示例 ==========");
            showColumnComments("scm_supplier");

            log.info("\n========================================");
            log.info("  注释修复完成！");
            log.info("========================================");

        } catch (Exception e) {
            log.error("修复注释失败", e);
        }
    }

    private void showTableComments() {
        String sql = "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 'scm_%' ORDER BY TABLE_NAME";
        
        log.info("{:<45} | {}", "表名", "表注释");
        log.info("--------------------------------------------------------------------------------");
        
        try {
            jdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    String name = rs.getString(1);
                    String comment = rs.getString(2);
                    log.info("{:<45} | {}", name, comment);
                }
                return null;
            });
        } catch (Exception e) {
            log.error("查询表注释失败", e);
        }
    }

    private void showColumnComments(String tableName) {
        String sql = "SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
        
        log.info("\n表: {}", tableName);
        log.info("  {:<25} | {}", "字段名", "字段注释");
        log.info("  ---------------------------------------------------------");
        
        try {
            jdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    String name = rs.getString(1);
                    String comment = rs.getString(2);
                    log.info("  {:<25} | {}", name, comment);
                }
                return null;
            }, tableName);
        } catch (Exception e) {
            log.error("查询字段注释失败", e);
        }
    }

    private List<String> getFixSqls() {
        List<String> sqls = new ArrayList<>();

        sqls.add("ALTER TABLE scm_supplier COMMENT = '供应商基础信息表'");
        sqls.add("ALTER TABLE scm_supplier_qualification COMMENT = '供应商资质审核表'");
        sqls.add("ALTER TABLE scm_qualification_alert COMMENT = '资质预警记录表'");
        sqls.add("ALTER TABLE scm_supplier_classification_log COMMENT = '供应商分级分类变更记录表'");

        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `supplier_code` VARCHAR(50) NOT NULL COMMENT '供应商编码'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `supplier_name` VARCHAR(200) NOT NULL COMMENT '供应商名称'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `supplier_type` TINYINT COMMENT '供应商类型：1生产型 2贸易型 3服务型'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `grade` TINYINT COMMENT '供应商等级：1A级 2AA级 3AAA级'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `material_category` TINYINT COMMENT '物资类别：1原材料 2辅料 3设备'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `cooperation_level` TINYINT COMMENT '合作分级：1战略 2合格 3潜在'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `contact_person` VARCHAR(50) COMMENT '联系人'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `contact_phone` VARCHAR(20) COMMENT '联系电话'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `contact_email` VARCHAR(100) COMMENT '联系邮箱'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `address` VARCHAR(500) COMMENT '详细地址'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `status` TINYINT COMMENT '状态：0待准入 1已准入 2已冻结 3已淘汰'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `audit_status` TINYINT COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `audit_remark` VARCHAR(500) COMMENT '审核备注'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `remark` VARCHAR(1000) COMMENT '备注'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `update_by` VARCHAR(50) COMMENT '更新人'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `update_time` DATETIME COMMENT '更新时间'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `ext1` VARCHAR(255) COMMENT '预留扩展字段1'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `ext2` VARCHAR(255) COMMENT '预留扩展字段2'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `ext3` VARCHAR(255) COMMENT '预留扩展字段3'");

        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `qualification_type` VARCHAR(50) COMMENT '资质类型：BUSINESS_LICENSE营业执照, TAX_REGISTRATION税务登记证, ORG_CODE组织机构代码证, PRODUCT_CERT产品认证, QUALITY_CERT质量认证, OTHER其他'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `qualification_name` VARCHAR(200) COMMENT '资质名称'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `certificate_no` VARCHAR(100) COMMENT '证书编号'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `issue_date` DATE COMMENT '发证日期'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `expiry_date` DATE COMMENT '有效期至'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `is_long_term` TINYINT COMMENT '是否长期有效：0否 1是'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `file_urls` TEXT COMMENT '附件URL列表，多个用逗号分隔'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `issuing_authority` VARCHAR(200) COMMENT '发证机关'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_status` TINYINT COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_remark` VARCHAR(1000) COMMENT '审核备注'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_by` VARCHAR(50) COMMENT '审核人'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_time` DATETIME COMMENT '审核时间'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `alert_status` TINYINT COMMENT '预警状态：0正常 1即将到期 2已过期'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `alert_sent` TINYINT COMMENT '是否已发送预警：0否 1是'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `remark` VARCHAR(1000) COMMENT '备注'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `update_by` VARCHAR(50) COMMENT '更新人'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `update_time` DATETIME COMMENT '更新时间'");

        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `qualification_id` BIGINT COMMENT '资质ID'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_type` TINYINT COMMENT '预警类型：1即将到期 2已过期'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_title` VARCHAR(200) COMMENT '预警标题'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_content` VARCHAR(1000) COMMENT '预警内容'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_date` DATE COMMENT '预警日期'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `days_before_expiry` INT COMMENT '到期前天数（即将到期预警时有效）'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `read_time` DATETIME COMMENT '阅读时间'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'");

        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `old_material_category` TINYINT COMMENT '原物资类别'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `new_material_category` TINYINT COMMENT '新物资类别'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `old_cooperation_level` TINYINT COMMENT '原合作分级'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `new_cooperation_level` TINYINT COMMENT '新合作分级'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `change_reason` VARCHAR(500) COMMENT '变更原因'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'");

        return sqls;
    }
}
