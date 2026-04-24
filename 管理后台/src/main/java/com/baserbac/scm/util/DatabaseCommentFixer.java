package com.baserbac.scm.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库注释修复工具
 * 用于修复SCM模块表及字段的中文注释乱码问题
 * 
 * 执行方式:
 * 1. 修改下方的数据库连接配置
 * 2. 使用Maven执行: mvn exec:java -Dexec.mainClass="com.baserbac.scm.util.DatabaseCommentFixer"
 * 3. 或者编译后直接运行: java -cp target/classes com.baserbac.scm.util.DatabaseCommentFixer
 */
public class DatabaseCommentFixer {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/small_tweaks_scm?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   SCM数据库中文注释修复工具");
        System.out.println("========================================");

        String password = DB_PASSWORD;
        if (args.length > 0) {
            password = args[0];
            System.out.println("使用命令行传入的密码");
        } else if (DB_PASSWORD == null || DB_PASSWORD.isEmpty()) {
            System.out.println("警告: 数据库密码未配置");
            System.out.println("请修改代码中的DB_PASSWORD，或通过命令行参数传入密码");
            System.out.println("使用方式: java DatabaseCommentFixer <password>");
            return;
        }

        System.out.println("连接数据库: " + DB_URL);
        System.out.println("用户: " + DB_USER);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, password)) {
            conn.setAutoCommit(false);
            
            System.out.println("数据库连接成功");
            System.out.println("开始执行注释修复...");

            executeSetNames(conn);
            
            List<String> sqls = getFixSqls();
            int successCount = 0;
            int failCount = 0;
            List<String> failedSqls = new ArrayList<>();

            for (String sql : sqls) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    successCount++;
                    System.out.println("✓ 执行成功: " + truncateSql(sql, 60));
                } catch (SQLException e) {
                    failCount++;
                    failedSqls.add(sql);
                    System.out.println("✗ 执行失败: " + truncateSql(sql, 60));
                    System.out.println("  错误: " + e.getMessage());
                }
            }

            conn.commit();
            System.out.println("");
            System.out.println("========================================");
            System.out.println("执行完成!");
            System.out.println("成功: " + successCount + " 条");
            System.out.println("失败: " + failCount + " 条");
            
            if (!failedSqls.isEmpty()) {
                System.out.println("");
                System.out.println("失败的SQL语句:");
                for (String sql : failedSqls) {
                    System.out.println("  - " + truncateSql(sql, 80));
                }
            }
            System.out.println("========================================");

        } catch (SQLException e) {
            System.err.println("数据库连接或执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executeSetNames(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET NAMES utf8mb4");
            stmt.execute("SET CHARACTER SET utf8mb4");
            System.out.println("设置字符集: utf8mb4");
        }
    }

    private static String truncateSql(String sql, int maxLength) {
        if (sql == null) return "";
        if (sql.length() <= maxLength) return sql;
        return sql.substring(0, maxLength) + "...";
    }

    private static List<String> getFixSqls() {
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
