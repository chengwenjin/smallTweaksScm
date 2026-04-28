package com.baserbac.scm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SCM数据库初始化器
 * 在应用启动时执行数据库表结构初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScmDatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("检查SCM数据库表结构...");
        
        try {
            jdbcTemplate.execute("SET NAMES utf8mb4");
            jdbcTemplate.execute("SET CHARACTER SET utf8mb4");
            
            checkAndAddColumn("scm_qualification_alert", "create_by", "VARCHAR(50) COMMENT '创建人'");
            checkAndAddColumn("scm_supplier", "is_deleted", "TINYINT DEFAULT 0 COMMENT '软删除'");
            checkAndAddColumn("scm_supplier_qualification", "is_deleted", "TINYINT DEFAULT 0 COMMENT '软删除'");
            checkAndAddColumn("scm_qualification_alert", "is_deleted", "TINYINT DEFAULT 0 COMMENT '软删除'");
            checkAndAddColumn("scm_supplier_classification_log", "is_deleted", "TINYINT DEFAULT 0 COMMENT '软删除'");
            
            checkAndCreateBlacklistTable();
            
            checkAndCreateInquiryTables();
            
            checkAndCreatePerformanceTables();
            
            checkAndCreatePurchaseCollaborationTables();
            
            log.info("SCM数据库表结构检查完成");
        } catch (Exception e) {
            log.warn("检查表结构失败: {}", e.getMessage());
        }
    }

    private void checkAndCreateBlacklistTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_supplier_blacklist'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_supplier_blacklist (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        supplier_id BIGINT COMMENT '供应商ID',
                        supplier_code VARCHAR(50) COMMENT '供应商编码',
                        supplier_name VARCHAR(200) COMMENT '供应商名称',
                        blacklist_type TINYINT COMMENT '黑名单类型：1严重违约 2质量问题 3欺诈行为 4其他',
                        blacklist_reason VARCHAR(1000) COMMENT '列入原因',
                        blacklist_date DATE COMMENT '列入日期',
                        is_permanent TINYINT COMMENT '是否永久：0否 1是',
                        expire_date DATE COMMENT '到期日期',
                        status TINYINT DEFAULT 1 COMMENT '状态：1在黑名单 2已移除',
                        remove_reason VARCHAR(1000) COMMENT '移除原因',
                        remove_date DATE COMMENT '移除日期',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        remark VARCHAR(1000) COMMENT '备注',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_supplier_id (supplier_id),
                        KEY idx_supplier_code (supplier_code),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商黑名单表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建黑名单表成功: scm_supplier_blacklist");
            } else {
                log.debug("黑名单表已存在: scm_supplier_blacklist");
            }
            
            insertBlacklistTestData();
            
        } catch (Exception e) {
            log.error("检查或创建黑名单表失败", e);
        }
    }

    private void checkAndCreateInquiryTables() {
        try {
            checkAndCreateRequirementTable();
            checkAndCreateInquiryTable();
            checkAndCreateInquirySupplierTable();
            checkAndCreateComparisonTable();
            checkAndCreateTenderTable();
            checkAndCreateTenderBidTable();
            
            insertInquiryTestData();
            
        } catch (Exception e) {
            log.error("检查或创建询价招投标表失败", e);
        }
    }

    private void checkAndCreateRequirementTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_purchase_requirement'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_purchase_requirement (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        req_no VARCHAR(50) COMMENT '需求单编号',
                        req_name VARCHAR(200) COMMENT '需求单名称',
                        material_name VARCHAR(200) COMMENT '物资名称',
                        material_spec VARCHAR(500) COMMENT '物资规格',
                        material_unit VARCHAR(20) COMMENT '物资单位',
                        quantity DECIMAL(18,2) COMMENT '需求数量',
                        required_date DATE COMMENT '需求日期',
                        urgency TINYINT COMMENT '紧急程度：1普通 2紧急 3特急',
                        budget_range VARCHAR(100) COMMENT '预算范围',
                        description TEXT COMMENT '需求描述',
                        status TINYINT DEFAULT 0 COMMENT '状态：0待审批 1审批通过 2已询价 3已比价 4已采购 5已完成 6已取消',
                        req_dept VARCHAR(100) COMMENT '需求部门',
                        req_person VARCHAR(50) COMMENT '需求人',
                        req_phone VARCHAR(20) COMMENT '联系电话',
                        delivery_date DATE COMMENT '期望交货日期',
                        delivery_address VARCHAR(500) COMMENT '交货地址',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_req_no (req_no),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购需求单表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建采购需求单表成功: scm_purchase_requirement");
            } else {
                log.debug("采购需求单表已存在: scm_purchase_requirement");
            }
        } catch (Exception e) {
            log.error("检查或创建采购需求单表失败", e);
        }
    }

    private void checkAndCreateInquiryTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_inquiry'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_inquiry (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        inquiry_no VARCHAR(50) COMMENT '询价单编号',
                        inquiry_name VARCHAR(200) COMMENT '询价单名称',
                        req_ids VARCHAR(500) COMMENT '采购需求单ID列表',
                        status TINYINT DEFAULT 0 COMMENT '状态：0待发布 1已发布 2报价中 3报价结束 4已比价 5已取消',
                        deadline DATE COMMENT '报价截止日期',
                        contact_person VARCHAR(50) COMMENT '联系人',
                        contact_phone VARCHAR(20) COMMENT '联系电话',
                        contact_email VARCHAR(100) COMMENT '联系邮箱',
                        description TEXT COMMENT '询价说明',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_inquiry_no (inquiry_no),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询价单表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建询价单表成功: scm_inquiry");
            } else {
                log.debug("询价单表已存在: scm_inquiry");
            }
        } catch (Exception e) {
            log.error("检查或创建询价单表失败", e);
        }
    }

    private void checkAndCreateInquirySupplierTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_inquiry_supplier'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_inquiry_supplier (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        inquiry_id BIGINT COMMENT '询价单ID',
                        supplier_id BIGINT COMMENT '供应商ID',
                        supplier_name VARCHAR(200) COMMENT '供应商名称',
                        invite_status TINYINT DEFAULT 0 COMMENT '邀请状态：0待邀请 1已邀请 2已拒绝',
                        quote_status TINYINT DEFAULT 0 COMMENT '报价状态：0未报价 1已报价 2已撤回',
                        unit_price DECIMAL(18,2) COMMENT '单价',
                        total_price DECIMAL(18,2) COMMENT '总价',
                        delivery_date DATE COMMENT '交货日期',
                        payment_terms VARCHAR(200) COMMENT '付款条件',
                        warranty VARCHAR(200) COMMENT '质保期',
                        quote_remark TEXT COMMENT '报价说明',
                        quote_time DATETIME COMMENT '报价时间',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_inquiry_id (inquiry_id),
                        KEY idx_supplier_id (supplier_id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询价供应商关联表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建询价供应商关联表成功: scm_inquiry_supplier");
            } else {
                log.debug("询价供应商关联表已存在: scm_inquiry_supplier");
            }
        } catch (Exception e) {
            log.error("检查或创建询价供应商关联表失败", e);
        }
    }

    private void checkAndCreateComparisonTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_price_comparison'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_price_comparison (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        inquiry_id BIGINT COMMENT '询价单ID',
                        comparison_no VARCHAR(50) COMMENT '比价单编号',
                        comparison_name VARCHAR(200) COMMENT '比价单名称',
                        req_id BIGINT COMMENT '采购需求单ID',
                        req_name VARCHAR(200) COMMENT '采购需求单名称',
                        material_name VARCHAR(200) COMMENT '物资名称',
                        material_spec VARCHAR(500) COMMENT '物资规格',
                        material_unit VARCHAR(20) COMMENT '物资单位',
                        req_quantity DECIMAL(18,2) COMMENT '需求数量',
                        status TINYINT DEFAULT 0 COMMENT '状态：0待比价 1比价中 2已完成 3已取消',
                        recommend_supplier_id BIGINT COMMENT '推荐供应商ID',
                        recommend_supplier_name VARCHAR(200) COMMENT '推荐供应商名称',
                        recommend_price DECIMAL(18,2) COMMENT '推荐价格',
                        recommend_reason VARCHAR(500) COMMENT '推荐原因',
                        comparison_result TEXT COMMENT '比价结果',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_comparison_no (comparison_no),
                        KEY idx_inquiry_id (inquiry_id),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='比价单表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建比价单表成功: scm_price_comparison");
            } else {
                log.debug("比价单表已存在: scm_price_comparison");
            }
        } catch (Exception e) {
            log.error("检查或创建比价单表失败", e);
        }
    }

    private void checkAndCreateTenderTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_tender'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_tender (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        tender_no VARCHAR(50) COMMENT '招标单编号',
                        tender_name VARCHAR(200) COMMENT '招标单名称',
                        tender_type TINYINT DEFAULT 1 COMMENT '招标类型：1公开招标 2邀请招标',
                        description TEXT COMMENT '招标描述',
                        req_ids VARCHAR(500) COMMENT '采购需求单ID列表',
                        estimated_budget DECIMAL(18,2) COMMENT '预算金额',
                        publish_date DATE COMMENT '发布日期',
                        bid_deadline DATE COMMENT '投标截止日期',
                        open_date DATE COMMENT '开标日期',
                        contact_person VARCHAR(50) COMMENT '联系人',
                        contact_phone VARCHAR(20) COMMENT '联系电话',
                        contact_email VARCHAR(100) COMMENT '联系邮箱',
                        tender_docs TEXT COMMENT '招标文件URL列表',
                        tender_address VARCHAR(500) COMMENT '招标地址',
                        status TINYINT DEFAULT 0 COMMENT '状态：0待发布 1招标中 2投标中 3已开标 4评标中 5已定标 6已取消',
                        win_supplier_id BIGINT COMMENT '中标供应商ID',
                        win_supplier_name VARCHAR(200) COMMENT '中标供应商名称',
                        win_price DECIMAL(18,2) COMMENT '中标价格',
                        win_reason VARCHAR(500) COMMENT '中标原因',
                        win_date DATE COMMENT '中标日期',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_tender_no (tender_no),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招标单表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建招标单表成功: scm_tender");
            } else {
                log.debug("招标单表已存在: scm_tender");
            }
        } catch (Exception e) {
            log.error("检查或创建招标单表失败", e);
        }
    }

    private void checkAndCreateTenderBidTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_tender_bid'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_tender_bid (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        tender_id BIGINT COMMENT '招标单ID',
                        supplier_id BIGINT COMMENT '供应商ID',
                        supplier_name VARCHAR(200) COMMENT '供应商名称',
                        bid_price DECIMAL(18,2) COMMENT '投标价格',
                        bid_time DATETIME COMMENT '投标时间',
                        bid_docs TEXT COMMENT '投标文件URL列表',
                        bid_description TEXT COMMENT '投标说明',
                        technical_params TEXT COMMENT '技术参数',
                        delivery_plan TEXT COMMENT '交货计划',
                        after_sales_service TEXT COMMENT '售后服务',
                        is_win TINYINT DEFAULT 0 COMMENT '是否中标：0否 1是',
                        win_reason VARCHAR(500) COMMENT '中标原因',
                        score INT COMMENT '得分',
                        evaluate_remark VARCHAR(1000) COMMENT '评标备注',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_tender_id (tender_id),
                        KEY idx_supplier_id (supplier_id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投标单表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建投标单表成功: scm_tender_bid");
            } else {
                log.debug("投标单表已存在: scm_tender_bid");
            }
        } catch (Exception e) {
            log.error("检查或创建投标单表失败", e);
        }
    }

    private void checkAndCreatePerformanceTables() {
        try {
            checkAndCreateKpiTable();
            checkAndCreateReportTable();
            
            insertPerformanceTestData();
            
        } catch (Exception e) {
            log.error("检查或创建绩效考核表失败", e);
        }
    }

    private void checkAndCreateKpiTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_supplier_kpi'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_supplier_kpi (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        supplier_id BIGINT COMMENT '供应商ID',
                        supplier_name VARCHAR(200) COMMENT '供应商名称',
                        period_type TINYINT COMMENT '周期类型：1月度 2季度 3年度',
                        year INT COMMENT '年份',
                        quarter INT COMMENT '季度',
                        month INT COMMENT '月份',
                        delivery_on_time_rate DECIMAL(10,2) COMMENT '交付准时率',
                        delivery_total_count INT COMMENT '交付总次数',
                        delivery_on_time_count INT COMMENT '交付准时次数',
                        quality_pass_rate DECIMAL(10,2) COMMENT '来料质检合格率',
                        quality_total_count INT COMMENT '质检总次数',
                        quality_pass_count INT COMMENT '质检合格次数',
                        price_competitiveness DECIMAL(10,2) COMMENT '价格竞争力',
                        price_compare_count INT COMMENT '价格对比次数',
                        price_best_count INT COMMENT '价格最优次数',
                        service_response_speed DECIMAL(10,2) COMMENT '售后服务响应速度',
                        service_total_count INT COMMENT '服务总次数',
                        service_response_on_time_count INT COMMENT '服务响应及时次数',
                        total_score DECIMAL(10,2) COMMENT '综合评分',
                        grade TINYINT COMMENT '等级：1A级 2AA级 3AAA级',
                        status TINYINT DEFAULT 1 COMMENT '状态',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        remark VARCHAR(1000) COMMENT '备注',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_supplier_id (supplier_id),
                        KEY idx_period (year, period_type, quarter)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商KPI记录表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建供应商KPI表成功: scm_supplier_kpi");
            } else {
                log.debug("供应商KPI表已存在: scm_supplier_kpi");
            }
        } catch (Exception e) {
            log.error("检查或创建KPI表失败", e);
        }
    }

    private void checkAndCreateReportTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_supplier_performance_report'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_supplier_performance_report (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        report_no VARCHAR(50) COMMENT '报告编号',
                        report_name VARCHAR(200) COMMENT '报告名称',
                        report_type TINYINT COMMENT '报告类型：1季度 2年度',
                        year INT COMMENT '年份',
                        quarter INT COMMENT '季度',
                        supplier_id BIGINT COMMENT '供应商ID',
                        supplier_name VARCHAR(200) COMMENT '供应商名称',
                        delivery_on_time_rate DECIMAL(10,2) COMMENT '交付准时率',
                        quality_pass_rate DECIMAL(10,2) COMMENT '来料质检合格率',
                        price_competitiveness DECIMAL(10,2) COMMENT '价格竞争力',
                        service_response_speed DECIMAL(10,2) COMMENT '售后服务响应速度',
                        total_score DECIMAL(10,2) COMMENT '综合评分',
                        grade TINYINT COMMENT '等级：1A级 2AA级 3AAA级',
                        ranking INT COMMENT '排名',
                        total_suppliers INT COMMENT '总供应商数',
                        previous_delivery_rate DECIMAL(10,2) COMMENT '上期交付准时率',
                        previous_quality_rate DECIMAL(10,2) COMMENT '上期质检合格率',
                        previous_price_score DECIMAL(10,2) COMMENT '上期价格竞争力',
                        previous_service_score DECIMAL(10,2) COMMENT '上期服务响应速度',
                        previous_total_score DECIMAL(10,2) COMMENT '上期综合评分',
                        previous_grade TINYINT COMMENT '上期等级',
                        previous_ranking INT COMMENT '上期排名',
                        quota_suggestion DECIMAL(5,2) COMMENT '配额建议',
                        strength_analysis TEXT COMMENT '优势分析',
                        weakness_analysis TEXT COMMENT '劣势分析',
                        improvement_suggestion TEXT COMMENT '改进建议',
                        start_date DATE COMMENT '统计开始日期',
                        end_date DATE COMMENT '统计结束日期',
                        status TINYINT DEFAULT 1 COMMENT '状态',
                        approved_by VARCHAR(50) COMMENT '审批人',
                        approve_time DATETIME COMMENT '审批时间',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        remark VARCHAR(1000) COMMENT '备注',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_report_no (report_no),
                        KEY idx_supplier_id (supplier_id),
                        KEY idx_period (year, report_type, quarter)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商绩效报告表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建供应商绩效报告表成功: scm_supplier_performance_report");
            } else {
                log.debug("供应商绩效报告表已存在: scm_supplier_performance_report");
            }
        } catch (Exception e) {
            log.error("检查或创建绩效报告表失败", e);
        }
    }

    private void insertPerformanceTestData() {
        try {
            List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                "SELECT id, supplier_code, supplier_name FROM scm_supplier WHERE is_deleted = 0 ORDER BY id"
            );
            
            if (suppliers.isEmpty()) {
                log.warn("没有供应商数据，无法生成绩效考核测试数据");
                return;
            }
            
            String kpiCountSql = "SELECT COUNT(*) FROM scm_supplier_kpi WHERE is_deleted = 0";
            Integer kpiCount = jdbcTemplate.queryForObject(kpiCountSql, Integer.class);
            
            if (kpiCount == null || kpiCount < 30) {
                insertKpiTestData(suppliers);
            } else {
                log.info("KPI测试数据已存在: {}条", kpiCount);
            }
            
            String reportCountSql = "SELECT COUNT(*) FROM scm_supplier_performance_report WHERE is_deleted = 0";
            Integer reportCount = jdbcTemplate.queryForObject(reportCountSql, Integer.class);
            
            if (reportCount == null || reportCount < 30) {
                insertReportTestData(suppliers);
            } else {
                log.info("绩效报告测试数据已存在: {}条", reportCount);
            }
            
        } catch (Exception e) {
            log.error("插入绩效考核测试数据失败", e);
        }
    }

    private void insertKpiTestData(List<Map<String, Object>> suppliers) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        
        String insertKpiSql = """
            INSERT INTO scm_supplier_kpi 
            (supplier_id, supplier_name, period_type, year, quarter, month,
             delivery_on_time_rate, delivery_total_count, delivery_on_time_count,
             quality_pass_rate, quality_total_count, quality_pass_count,
             price_competitiveness, price_compare_count, price_best_count,
             service_response_speed, service_total_count, service_response_on_time_count,
             total_score, grade, status, is_deleted, create_by, remark)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        int inserted = 0;
        
        for (int i = 0; i < Math.min(5, suppliers.size()); i++) {
            Map<String, Object> supplier = suppliers.get(i);
            Long supplierId = ((Number) supplier.get("id")).longValue();
            String supplierName = (String) supplier.get("supplier_name");
            
            for (int q = 1; q <= 4; q++) {
                for (int m = 1; m <= 3; m++) {
                    int month = (q - 1) * 3 + m;
                    
                    double baseRate = 80 + Math.random() * 20;
                    double qualityRate = 85 + Math.random() * 15;
                    double priceScore = 70 + Math.random() * 25;
                    double serviceScore = 75 + Math.random() * 20;
                    
                    BigDecimal deliveryRate = new BigDecimal(baseRate).setScale(2, java.math.RoundingMode.HALF_UP);
                    BigDecimal qualityPassRate = new BigDecimal(qualityRate).setScale(2, java.math.RoundingMode.HALF_UP);
                    BigDecimal priceCompetitiveness = new BigDecimal(priceScore).setScale(2, java.math.RoundingMode.HALF_UP);
                    BigDecimal serviceResponseSpeed = new BigDecimal(serviceScore).setScale(2, java.math.RoundingMode.HALF_UP);
                    
                    int deliveryTotal = 50 + (int) (Math.random() * 50);
                    int deliveryOnTime = (int) (deliveryTotal * baseRate / 100);
                    int qualityTotal = 30 + (int) (Math.random() * 30);
                    int qualityPass = (int) (qualityTotal * qualityRate / 100);
                    int priceCompare = 10 + (int) (Math.random() * 15);
                    int priceBest = (int) (priceCompare * priceScore / 100);
                    int serviceTotal = 8 + (int) (Math.random() * 12);
                    int serviceResponse = (int) (serviceTotal * serviceScore / 100);
                    
                    BigDecimal totalScore = deliveryRate.multiply(new BigDecimal("0.30"))
                        .add(qualityPassRate.multiply(new BigDecimal("0.30")))
                        .add(priceCompetitiveness.multiply(new BigDecimal("0.20")))
                        .add(serviceResponseSpeed.multiply(new BigDecimal("0.20")))
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                    
                    int grade;
                    if (totalScore.compareTo(new BigDecimal("90")) >= 0) {
                        grade = 3;
                    } else if (totalScore.compareTo(new BigDecimal("80")) >= 0) {
                        grade = 2;
                    } else {
                        grade = 1;
                    }
                    
                    String remark = supplierName + " - " + (currentYear - 1) + "年第" + q + "季度" + month + "月KPI数据";
                    
                    try {
                        jdbcTemplate.update(insertKpiSql,
                            supplierId, supplierName, 1, currentYear - 1, q, month,
                            deliveryRate, deliveryTotal, deliveryOnTime,
                            qualityPassRate, qualityTotal, qualityPass,
                            priceCompetitiveness, priceCompare, priceBest,
                            serviceResponseSpeed, serviceTotal, serviceResponse,
                            totalScore, grade, 1, 0, "admin", remark
                        );
                        inserted++;
                    } catch (Exception e) {
                        log.warn("插入KPI测试数据失败", e);
                    }
                }
            }
            
            for (int q = 1; q <= 4; q++) {
                double baseRate = 82 + Math.random() * 18;
                double qualityRate = 87 + Math.random() * 13;
                double priceScore = 72 + Math.random() * 23;
                double serviceScore = 78 + Math.random() * 18;
                
                BigDecimal deliveryRate = new BigDecimal(baseRate).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal qualityPassRate = new BigDecimal(qualityRate).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal priceCompetitiveness = new BigDecimal(priceScore).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal serviceResponseSpeed = new BigDecimal(serviceScore).setScale(2, java.math.RoundingMode.HALF_UP);
                
                int deliveryTotal = 150 + (int) (Math.random() * 100);
                int deliveryOnTime = (int) (deliveryTotal * baseRate / 100);
                int qualityTotal = 90 + (int) (Math.random() * 60);
                int qualityPass = (int) (qualityTotal * qualityRate / 100);
                int priceCompare = 30 + (int) (Math.random() * 30);
                int priceBest = (int) (priceCompare * priceScore / 100);
                int serviceTotal = 25 + (int) (Math.random() * 25);
                int serviceResponse = (int) (serviceTotal * serviceScore / 100);
                
                BigDecimal totalScore = deliveryRate.multiply(new BigDecimal("0.30"))
                    .add(qualityPassRate.multiply(new BigDecimal("0.30")))
                    .add(priceCompetitiveness.multiply(new BigDecimal("0.20")))
                    .add(serviceResponseSpeed.multiply(new BigDecimal("0.20")))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
                
                int grade;
                if (totalScore.compareTo(new BigDecimal("90")) >= 0) {
                    grade = 3;
                } else if (totalScore.compareTo(new BigDecimal("80")) >= 0) {
                    grade = 2;
                } else {
                    grade = 1;
                }
                
                String remark = supplierName + " - " + (currentYear - 1) + "年第" + q + "季度KPI数据";
                
                try {
                    jdbcTemplate.update(insertKpiSql,
                        supplierId, supplierName, 2, currentYear - 1, q, null,
                        deliveryRate, deliveryTotal, deliveryOnTime,
                        qualityPassRate, qualityTotal, qualityPass,
                        priceCompetitiveness, priceCompare, priceBest,
                        serviceResponseSpeed, serviceTotal, serviceResponse,
                        totalScore, grade, 1, 0, "admin", remark
                    );
                    inserted++;
                } catch (Exception e) {
                    log.warn("插入季度KPI测试数据失败", e);
                }
            }
        }
        
        log.info("已插入{}条KPI测试数据", inserted);
    }

    private void insertReportTestData(List<Map<String, Object>> suppliers) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        
        String insertReportSql = """
            INSERT INTO scm_supplier_performance_report 
            (report_no, report_name, report_type, year, quarter,
             supplier_id, supplier_name,
             delivery_on_time_rate, quality_pass_rate, price_competitiveness, service_response_speed,
             total_score, grade, ranking, total_suppliers,
             previous_delivery_rate, previous_quality_rate, previous_price_score, previous_service_score,
             previous_total_score, previous_grade, previous_ranking,
             quota_suggestion, strength_analysis, weakness_analysis, improvement_suggestion,
             start_date, end_date, status, is_deleted, create_by, remark)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        int totalSuppliers = suppliers.size();
        int inserted = 0;
        
        for (int i = 0; i < Math.min(5, suppliers.size()); i++) {
            Map<String, Object> supplier = suppliers.get(i);
            Long supplierId = ((Number) supplier.get("id")).longValue();
            String supplierName = (String) supplier.get("supplier_name");
            
            for (int q = 1; q <= 4; q++) {
                String reportNo = "RPT-Q" + (currentYear - 1) + q + String.format("%04d", i + 1);
                String reportName = supplierName + " - " + (currentYear - 1) + "年第" + q + "季度绩效报告";
                
                double baseRate = 82 + Math.random() * 18;
                double qualityRate = 87 + Math.random() * 13;
                double priceScore = 72 + Math.random() * 23;
                double serviceScore = 78 + Math.random() * 18;
                
                BigDecimal deliveryRate = new BigDecimal(baseRate).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal qualityPassRate = new BigDecimal(qualityRate).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal priceCompetitiveness = new BigDecimal(priceScore).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal serviceResponseSpeed = new BigDecimal(serviceScore).setScale(2, java.math.RoundingMode.HALF_UP);
                
                BigDecimal totalScore = deliveryRate.multiply(new BigDecimal("0.30"))
                    .add(qualityPassRate.multiply(new BigDecimal("0.30")))
                    .add(priceCompetitiveness.multiply(new BigDecimal("0.20")))
                    .add(serviceResponseSpeed.multiply(new BigDecimal("0.20")))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
                
                int grade;
                if (totalScore.compareTo(new BigDecimal("90")) >= 0) {
                    grade = 3;
                } else if (totalScore.compareTo(new BigDecimal("80")) >= 0) {
                    grade = 2;
                } else {
                    grade = 1;
                }
                
                int ranking = 1 + (int) (Math.random() * totalSuppliers);
                
                double prevBaseRate = 80 + Math.random() * 20;
                double prevQualityRate = 85 + Math.random() * 15;
                double prevPriceScore = 70 + Math.random() * 25;
                double prevServiceScore = 75 + Math.random() * 20;
                
                BigDecimal prevDelivery = new BigDecimal(prevBaseRate).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal prevQuality = new BigDecimal(prevQualityRate).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal prevPrice = new BigDecimal(prevPriceScore).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal prevService = new BigDecimal(prevServiceScore).setScale(2, java.math.RoundingMode.HALF_UP);
                
                BigDecimal prevTotal = prevDelivery.multiply(new BigDecimal("0.30"))
                    .add(prevQuality.multiply(new BigDecimal("0.30")))
                    .add(prevPrice.multiply(new BigDecimal("0.20")))
                    .add(prevService.multiply(new BigDecimal("0.20")))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
                
                int prevGrade;
                if (prevTotal.compareTo(new BigDecimal("90")) >= 0) {
                    prevGrade = 3;
                } else if (prevTotal.compareTo(new BigDecimal("80")) >= 0) {
                    prevGrade = 2;
                } else {
                    prevGrade = 1;
                }
                
                int prevRanking = 1 + (int) (Math.random() * totalSuppliers);
                
                BigDecimal quotaSuggestion;
                if (ranking <= Math.ceil(totalSuppliers * 0.2)) {
                    quotaSuggestion = new BigDecimal("1.20");
                } else if (ranking <= Math.ceil(totalSuppliers * 0.5)) {
                    quotaSuggestion = new BigDecimal("1.00");
                } else if (ranking <= Math.ceil(totalSuppliers * 0.8)) {
                    quotaSuggestion = new BigDecimal("0.80");
                } else {
                    quotaSuggestion = new BigDecimal("0.50");
                }
                
                String strengthAnalysis = "";
                if (qualityPassRate.compareTo(new BigDecimal("90")) >= 0) {
                    strengthAnalysis = "产品质量稳定可靠，来料质检合格率较高；";
                }
                if (deliveryRate.compareTo(new BigDecimal("90")) >= 0) {
                    strengthAnalysis += "交付准时率高，供应链响应速度快；";
                }
                if (strengthAnalysis.isEmpty()) {
                    strengthAnalysis = "综合表现中等，各指标相对均衡；";
                }
                
                String weaknessAnalysis = "";
                if (priceCompetitiveness.compareTo(new BigDecimal("75")) < 0) {
                    weaknessAnalysis = "价格竞争力有待提升；";
                }
                if (serviceResponseSpeed.compareTo(new BigDecimal("80")) < 0) {
                    weaknessAnalysis += "售后服务响应速度较慢，客户体验待改善；";
                }
                if (weaknessAnalysis.isEmpty()) {
                    weaknessAnalysis = "整体表现良好，暂无明显短板；";
                }
                
                String improvementSuggestion = "建议持续关注供应链稳定性，加强与供应商的沟通协作；建议建立快速响应机制，优化售后服务流程，提升客户满意度；";
                
                int startMonth = (q - 1) * 3 + 1;
                int endMonth = q * 3;
                LocalDate startDate = LocalDate.of(currentYear - 1, startMonth, 1);
                LocalDate endDate = LocalDate.of(currentYear - 1, endMonth, LocalDate.of(currentYear - 1, endMonth, 1).lengthOfMonth());
                
                String remark = "测试数据 - " + supplierName + " " + (currentYear - 1) + "年Q" + q;
                
                try {
                    jdbcTemplate.update(insertReportSql,
                        reportNo, reportName, 1, currentYear - 1, q,
                        supplierId, supplierName,
                        deliveryRate, qualityPassRate, priceCompetitiveness, serviceResponseSpeed,
                        totalScore, grade, ranking, totalSuppliers,
                        prevDelivery, prevQuality, prevPrice, prevService,
                        prevTotal, prevGrade, prevRanking,
                        quotaSuggestion, strengthAnalysis, weaknessAnalysis, improvementSuggestion,
                        startDate, endDate, 1, 0, "admin", remark
                    );
                    inserted++;
                } catch (Exception e) {
                    log.warn("插入绩效报告测试数据失败", e);
                }
            }
        }
        
        log.info("已插入{}条绩效报告测试数据", inserted);
    }

    private void insertInquiryTestData() {
        try {
            String countSql = "SELECT COUNT(*) FROM scm_purchase_requirement WHERE is_deleted = 0";
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
            
            if (count == null || count < 30) {
                List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                    "SELECT id, supplier_code, supplier_name FROM scm_supplier WHERE is_deleted = 0 ORDER BY id"
                );
                
                if (suppliers.isEmpty()) {
                    log.warn("没有供应商数据，无法生成询价测试数据");
                } else {
                    insertRequirementTestData();
                    insertInquiryTestDataWithSuppliers(suppliers);
                    insertTenderTestData(suppliers);
                }
            }
            
            insertComparisonTestData();
            
        } catch (Exception e) {
            log.error("插入询价测试数据失败", e);
        }
    }
    
    private void insertComparisonTestData() {
        try {
            String countSql = "SELECT COUNT(*) FROM scm_price_comparison WHERE is_deleted = 0";
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
            
            if (count != null && count >= 30) {
                log.info("比价测试数据已存在: {}条", count);
                return;
            }
            
            List<Map<String, Object>> inquiries = jdbcTemplate.queryForList(
                "SELECT i.id, i.inquiry_no, i.inquiry_name, i.req_ids, r.req_name, r.material_name, r.material_spec, r.material_unit, r.quantity " +
                "FROM scm_inquiry i " +
                "LEFT JOIN scm_purchase_requirement r ON FIND_IN_SET(r.id, i.req_ids) > 0 " +
                "WHERE i.is_deleted = 0 " +
                "ORDER BY i.id"
            );
            
            List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                "SELECT id, supplier_name FROM scm_supplier WHERE is_deleted = 0 ORDER BY id"
            );
            
            if (inquiries.isEmpty() || suppliers.size() < 2) {
                log.warn("询价单或供应商数据不足，无法生成比价测试数据");
                return;
            }
            
            String insertComparisonSql = """
                INSERT INTO scm_price_comparison 
                (inquiry_id, comparison_no, comparison_name, req_id, req_name, material_name, 
                 material_spec, material_unit, req_quantity, status, 
                 recommend_supplier_id, recommend_supplier_name, recommend_price, recommend_reason, 
                 comparison_result, remark, is_deleted, create_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
                """;
            
            LocalDate today = LocalDate.now();
            String[] comparisonNames = {
                "不锈钢钢板采购比价", "铝合金型材采购比价", "电子元器件采购比价",
                "电机马达采购比价", "传感器采购比价", "控制器采购比价",
                "电缆线材采购比价", "密封件采购比价", "轴承采购比价",
                "润滑油采购比价", "化工原料采购比价", "包装材料采购比价",
                "紧固件采购比价", "液压元件采购比价", "气动元件采购比价",
                "电气开关采购比价", "变压器采购比价", "电容器采购比价",
                "电阻器采购比价", "连接器采购比价", "散热器采购比价",
                "风扇采购比价", "水泵采购比价", "阀门采购比价",
                "过滤器采购比价", "压力表采购比价", "流量计采购比价",
                "温度传感器采购比价", "压力传感器采购比价", "生产线设备采购比价"
            };
            
            int inserted = 0;
            
            for (int i = 0; i < 30; i++) {
                int inquiryIndex = i % inquiries.size();
                Map<String, Object> inquiry = inquiries.get(inquiryIndex);
                Long inquiryId = ((Number) inquiry.get("id")).longValue();
                String inquiryNo = (String) inquiry.get("inquiry_no");
                String materialName = (String) inquiry.get("material_name");
                
                Long reqId = null;
                String reqName = null;
                String materialSpec = (String) inquiry.get("material_spec");
                String materialUnit = (String) inquiry.get("material_unit");
                java.math.BigDecimal reqQuantity = inquiry.get("quantity") != null ? 
                    new java.math.BigDecimal(inquiry.get("quantity").toString()) : 
                    new java.math.BigDecimal(100);
                
                if (inquiry.get("req_id") != null) {
                    reqId = ((Number) inquiry.get("req_id")).longValue();
                }
                if (inquiry.get("req_name") != null) {
                    reqName = (String) inquiry.get("req_name");
                }
                
                String comparisonNo = "CMP" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%05d", i + 1);
                String comparisonName = comparisonNames[i % comparisonNames.length];
                if (materialName != null && !materialName.isEmpty()) {
                    comparisonName = materialName + "采购比价单";
                }
                
                int recommendedSupplierIndex = i % suppliers.size();
                Map<String, Object> recommendedSupplier = suppliers.get(recommendedSupplierIndex);
                Long recommendedSupplierId = ((Number) recommendedSupplier.get("id")).longValue();
                String recommendedSupplierName = (String) recommendedSupplier.get("supplier_name");
                
                java.math.BigDecimal recommendPrice = new java.math.BigDecimal(10000 + i * 500);
                int status = i % 4;
                
                String recommendReason = "综合评分最高，价格最具竞争力";
                String comparisonResult = "比价完成，推荐供应商：" + recommendedSupplierName;
                String remark = "测试数据" + (i + 1);
                
                try {
                    jdbcTemplate.update(insertComparisonSql,
                        inquiryId, comparisonNo, comparisonName, reqId, reqName, materialName,
                        materialSpec, materialUnit, reqQuantity, status,
                        recommendedSupplierId, recommendedSupplierName, recommendPrice, recommendReason,
                        comparisonResult, remark
                    );
                    inserted++;
                    log.debug("插入比价测试数据: {}", comparisonName);
                } catch (Exception e) {
                    log.warn("插入比价测试数据失败: {} - {}", comparisonName, e.getMessage());
                }
            }
            
            log.info("已插入{}条比价测试数据", inserted);
            
        } catch (Exception e) {
            log.error("插入比价测试数据失败", e);
        }
    }

    private void insertRequirementTestData() {
        String[] materialNames = {
            "不锈钢钢板", "铝合金型材", "PVC管材", "电子元器件", "电机马达",
            "传感器", "控制器", "电缆线材", "密封件", "轴承",
            "润滑油", "化工原料", "包装材料", "紧固件", "液压元件",
            "气动元件", "电气开关", "变压器", "电容器", "电阻器",
            "连接器", "散热器", "风扇", "水泵", "阀门",
            "过滤器", "压力表", "流量计", "温度传感器", "压力传感器"
        };
        
        String[] reqNames = {
            "生产线升级改造采购需求", "原材料采购需求", "设备配件采购需求",
            "办公用品采购需求", "维护保养采购需求", "新设备采购需求",
            "备件采购需求", "应急采购需求", "季度采购需求", "年度采购需求",
            "项目采购需求", "研发采购需求", "生产采购需求", "质量检测设备采购",
            "安全设备采购", "环保设备采购", "IT设备采购", "办公家具采购",
            "车辆采购需求", "工具采购需求", "模具采购需求", "夹具采购需求",
            "量具采购需求", "刀具采购需求", "焊接材料采购", "油漆涂料采购",
            "胶粘剂采购", "清洁用品采购", "劳保用品采购", "消防用品采购"
        };
        
        String[] depts = {"生产部", "采购部", "技术部", "设备部", "质量部", "行政部", "研发部", "工程部"};
        String[] persons = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
        String[] phones = {"13800138001", "13800138002", "13800138003", "13800138004", "13800138005"};
        
        String insertSql = """
            INSERT INTO scm_purchase_requirement 
            (req_no, req_name, material_name, material_spec, material_unit, quantity, 
             required_date, urgency, budget_range, description, status, 
             req_dept, req_person, req_phone, delivery_date, delivery_address, 
             remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        LocalDate today = LocalDate.now();
        int inserted = 0;
        
        for (int i = 0; i < 30; i++) {
            String reqNo = "REQ" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%05d", i + 1);
            String materialName = materialNames[i % materialNames.length];
            String reqName = reqNames[i % reqNames.length] + " - " + materialName;
            String spec = "规格" + (i % 10 + 1);
            String unit = i % 3 == 0 ? "件" : (i % 3 == 1 ? "台" : "套");
            java.math.BigDecimal quantity = new java.math.BigDecimal(10 + i * 5);
            LocalDate requiredDate = today.plusDays(15 + i * 2);
            int urgency = (i % 3) + 1;
            String budget = (10000 + i * 5000) + " - " + (20000 + i * 10000) + "元";
            String description = "采购" + materialName + "，用于生产线维护和升级改造项目。";
            int status = i % 6;
            String dept = depts[i % depts.length];
            String person = persons[i % persons.length];
            String phone = phones[i % phones.length];
            LocalDate deliveryDate = requiredDate.minusDays(3);
            String address = "某某市某某区某某工业园区A栋" + (i % 10 + 1) + "号";
            String remark = "测试数据" + (i + 1);
            
            try {
                jdbcTemplate.update(insertSql,
                    reqNo, reqName, materialName, spec, unit, quantity,
                    requiredDate, urgency, budget, description, status,
                    dept, person, phone, deliveryDate, address,
                    remark
                );
                inserted++;
                log.debug("插入采购需求测试数据: {}", reqName);
            } catch (Exception e) {
                log.warn("插入采购需求测试数据失败: {} - {}", reqName, e.getMessage());
            }
        }
        
        log.info("已插入{}条采购需求测试数据", inserted);
    }

    private void insertInquiryTestDataWithSuppliers(List<Map<String, Object>> suppliers) {
        List<Map<String, Object>> requirements = jdbcTemplate.queryForList(
            "SELECT id, req_no, req_name, material_name FROM scm_purchase_requirement WHERE is_deleted = 0 ORDER BY id"
        );
        
        if (requirements.isEmpty()) {
            return;
        }
        
        String insertInquirySql = """
            INSERT INTO scm_inquiry 
            (inquiry_no, inquiry_name, req_ids, status, deadline, 
             contact_person, contact_phone, contact_email, description, 
             is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        String insertSupplierSql = """
            INSERT INTO scm_inquiry_supplier 
            (inquiry_id, supplier_id, supplier_name, invite_status, quote_status, 
             unit_price, total_price, delivery_date, payment_terms, warranty, 
             quote_remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        LocalDate today = LocalDate.now();
        String[] contactPersons = {"采购部-张三", "采购部-李四", "采购部-王五"};
        String[] contactPhones = {"010-88888801", "010-88888802", "010-88888803"};
        String[] emails = {"purchase1@company.com", "purchase2@company.com", "purchase3@company.com"};
        
        int inquiryInserted = 0;
        int supplierInserted = 0;
        
        for (int i = 0; i < Math.min(15, requirements.size()); i++) {
            Map<String, Object> req = requirements.get(i);
            Long reqId = ((Number) req.get("id")).longValue();
            String reqNo = (String) req.get("req_no");
            String reqName = (String) req.get("req_name");
            
            String inquiryNo = "INQ" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%05d", i + 1);
            String inquiryName = "询价单 - " + reqName;
            String reqIds = String.valueOf(reqId);
            int status = i % 5;
            LocalDate deadline = today.plusDays(7 + i * 2);
            String contactPerson = contactPersons[i % contactPersons.length];
            String contactPhone = contactPhones[i % contactPhones.length];
            String email = emails[i % emails.length];
            String description = "针对采购需求单" + reqNo + "的询价采购";
            
            try {
                jdbcTemplate.update(insertInquirySql,
                    inquiryNo, inquiryName, reqIds, status, deadline,
                    contactPerson, contactPhone, email, description
                );
                inquiryInserted++;
                
                List<Map<String, Object>> inquiryList = jdbcTemplate.queryForList(
                    "SELECT id FROM scm_inquiry WHERE inquiry_no = ?", inquiryNo
                );
                if (!inquiryList.isEmpty()) {
                    Long inquiryId = ((Number) inquiryList.get(0).get("id")).longValue();
                    
                    for (int j = 0; j < Math.min(3, suppliers.size()); j++) {
                        Map<String, Object> supplier = suppliers.get(j);
                        Long supplierId = ((Number) supplier.get("id")).longValue();
                        String supplierName = (String) supplier.get("supplier_name");
                        
                        int inviteStatus = 1;
                        int quoteStatus = i % 2 == 0 ? 1 : 0;
                        java.math.BigDecimal unitPrice = new java.math.BigDecimal(100 + i * 50 + j * 20);
                        java.math.BigDecimal totalPrice = unitPrice.multiply(new java.math.BigDecimal(10 + i * 2));
                        LocalDate deliveryDate = today.plusDays(15 + i * 3);
                        String paymentTerms = "月结30天";
                        String warranty = "12个月";
                        String quoteRemark = quoteStatus == 1 ? "报价已提交，价格优惠" : "待报价";
                        
                        try {
                            jdbcTemplate.update(insertSupplierSql,
                                inquiryId, supplierId, supplierName, inviteStatus, quoteStatus,
                                unitPrice, totalPrice, deliveryDate, paymentTerms, warranty,
                                quoteRemark
                            );
                            supplierInserted++;
                        } catch (Exception e) {
                            log.warn("插入询价供应商数据失败", e);
                        }
                    }
                }
                
            } catch (Exception e) {
                log.warn("插入询价单测试数据失败: {} - {}", inquiryName, e.getMessage());
            }
        }
        
        log.info("已插入{}条询价单测试数据，{}条询价供应商数据", inquiryInserted, supplierInserted);
    }

    private void insertTenderTestData(List<Map<String, Object>> suppliers) {
        String[] tenderNames = {
            "2024年生产线设备采购招标", "自动化控制系统招标", "仓储物流设备招标",
            "检测设备采购招标", "环保设备采购招标", "IT设备采购招标",
            "办公家具采购招标", "生产原材料年度采购招标", "维护备件采购招标",
            "能源管理系统招标", "安全监控系统招标", "通讯设备采购招标",
            "工业机器人采购招标", "生产线升级改造招标", "新厂区设备采购招标"
        };
        
        String insertTenderSql = """
            INSERT INTO scm_tender 
            (tender_no, tender_name, tender_type, description, estimated_budget, 
             publish_date, bid_deadline, open_date, contact_person, contact_phone, 
             contact_email, tender_address, status, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        String insertBidSql = """
            INSERT INTO scm_tender_bid 
            (tender_id, supplier_id, supplier_name, bid_price, bid_time, 
             bid_description, technical_params, delivery_plan, after_sales_service, 
             is_win, score, remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        LocalDate today = LocalDate.now();
        int tenderInserted = 0;
        int bidInserted = 0;
        
        for (int i = 0; i < 15; i++) {
            String tenderNo = "TND" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%05d", i + 1);
            String tenderName = tenderNames[i % tenderNames.length];
            int tenderType = (i % 2) + 1;
            String description = tenderName + "项目招标采购，欢迎合格供应商参与投标。";
            java.math.BigDecimal budget = new java.math.BigDecimal(500000 + i * 100000);
            LocalDate publishDate = today.minusDays(i * 3);
            LocalDate bidDeadline = publishDate.plusDays(15);
            LocalDate openDate = bidDeadline.plusDays(3);
            String contactPerson = "招标办-李工";
            String contactPhone = "010-88888888";
            String email = "tender@company.com";
            String address = "某某市某某区某某大厦A座10层招标会议室";
            int status = i % 6;
            
            try {
                jdbcTemplate.update(insertTenderSql,
                    tenderNo, tenderName, tenderType, description, budget,
                    publishDate, bidDeadline, openDate, contactPerson, contactPhone,
                    email, address, status
                );
                tenderInserted++;
                
                List<Map<String, Object>> tenderList = jdbcTemplate.queryForList(
                    "SELECT id FROM scm_tender WHERE tender_no = ?", tenderNo
                );
                if (!tenderList.isEmpty()) {
                    Long tenderId = ((Number) tenderList.get(0).get("id")).longValue();
                    
                    int winIndex = i % suppliers.size();
                    
                    for (int j = 0; j < Math.min(4, suppliers.size()); j++) {
                        Map<String, Object> supplier = suppliers.get(j);
                        Long supplierId = ((Number) supplier.get("id")).longValue();
                        String supplierName = (String) supplier.get("supplier_name");
                        
                        java.math.BigDecimal bidPrice = budget.multiply(new java.math.BigDecimal("0.8")).add(new java.math.BigDecimal(j * 10000));
                        LocalDateTime bidTime = LocalDateTime.now().minusDays(2 + i).withHour(10);
                        String bidDesc = "投标文件已提交，技术方案符合要求，价格有竞争力。";
                        String techParams = "技术参数符合招标文件要求，设备性能指标满足需求。";
                        String deliveryPlan = "合同签订后30天内完成交货，60天内完成安装调试。";
                        String afterSales = "提供12个月免费质保，终身维护服务。";
                        int isWin = j == winIndex ? 1 : 0;
                        int score = isWin == 1 ? 95 : (80 + j * 5);
                        String remark = isWin == 1 ? "中标" : "未中标";
                        
                        try {
                            jdbcTemplate.update(insertBidSql,
                                tenderId, supplierId, supplierName, bidPrice, bidTime,
                                bidDesc, techParams, deliveryPlan, afterSales,
                                isWin, score, remark
                            );
                            bidInserted++;
                            
                            if (isWin == 1) {
                                jdbcTemplate.update(
                                    "UPDATE scm_tender SET win_supplier_id = ?, win_supplier_name = ?, win_price = ?, win_reason = ?, win_date = ?, status = 5 WHERE id = ?",
                                    supplierId, supplierName, bidPrice, "综合评分第一", today, tenderId
                                );
                            }
                        } catch (Exception e) {
                            log.warn("插入投标数据失败", e);
                        }
                    }
                }
                
            } catch (Exception e) {
                log.warn("插入招标单测试数据失败: {} - {}", tenderName, e.getMessage());
            }
        }
        
        log.info("已插入{}条招标单测试数据，{}条投标数据", tenderInserted, bidInserted);
    }

    private void insertBlacklistTestData() {
        try {
            String countSql = "SELECT COUNT(*) FROM scm_supplier_blacklist WHERE is_deleted = 0";
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
            
            if (count != null && count >= 30) {
                log.info("黑名单测试数据已存在: {}条", count);
                return;
            }
            
            List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                "SELECT id, supplier_code, supplier_name FROM scm_supplier WHERE is_deleted = 0 ORDER BY id"
            );
            
            if (suppliers.isEmpty()) {
                log.warn("没有供应商数据，无法生成黑名单测试数据");
                return;
            }
            
            String[] reasons = {
                "严重违约行为",
                "提供虚假资质证书",
                "产品质量抽检不合格",
                "拖欠货款超过90天",
                "违反商业道德行为",
                "合同欺诈行为",
                "交付产品严重不合格",
                "拒绝履行合同义务",
                "供应商倒闭清算",
                "多次违反服务协议",
                "环保违规被处罚",
                "安全生产事故",
                "商业贿赂行为",
                "知识产权侵权",
                "数据安全违规"
            };
            
            String[] supplierNames = {
                "华东化工原料", "华南电子科技", "华北机械制造", "华中纺织服装",
                "西南食品加工", "西北建材供应", "东北汽车配件", "东南医疗器械",
                "上海精密仪器", "广州物流服务", "北京信息技术", "深圳新能源",
                "杭州智能制造", "南京环保科技", "武汉生物制药", "成都航空航天",
                "重庆汽车工业", "天津港口贸易", "青岛海洋装备", "厦门跨境电商",
                "大连船舶制造", "济南工程机械", "郑州轨道交通", "长沙新材料",
                "西安航天科技", "沈阳机床制造", "苏州芯片设计", "无锡光伏产业",
                "宁波港口物流", "福州软件服务"
            };
            
            String[] supplierCodes = {
                "BLACK001", "BLACK002", "BLACK003", "BLACK004", "BLACK005",
                "BLACK006", "BLACK007", "BLACK008", "BLACK009", "BLACK010",
                "BLACK011", "BLACK012", "BLACK013", "BLACK014", "BLACK015",
                "BLACK016", "BLACK017", "BLACK018", "BLACK019", "BLACK020",
                "BLACK021", "BLACK022", "BLACK023", "BLACK024", "BLACK025",
                "BLACK026", "BLACK027", "BLACK028", "BLACK029", "BLACK030"
            };
            
            String insertSql = """
                INSERT INTO scm_supplier_blacklist 
                (supplier_id, supplier_code, supplier_name, blacklist_type, blacklist_reason, 
                 blacklist_date, is_permanent, expire_date, status, remark, is_deleted, create_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
                """;
            
            int inserted = 0;
            LocalDate today = LocalDate.now();
            
            for (int i = 0; i < 30; i++) {
                int type = (i % 4) + 1;
                int isPermanent = (i % 3 == 0) ? 1 : 0;
                LocalDate blacklistDate = today.minusDays(30 + i * 5);
                LocalDate expireDate = null;
                if (isPermanent == 0) {
                    expireDate = today.plusDays(180 + i * 10);
                }
                int status = (i % 5 == 0) ? 2 : 1;
                
                String name;
                String code;
                Long supplierId;
                
                if (i < suppliers.size()) {
                    Map<String, Object> s = suppliers.get(i);
                    supplierId = ((Number) s.get("id")).longValue();
                    name = (String) s.get("supplier_name");
                    code = (String) s.get("supplier_code");
                } else {
                    supplierId = 100L + i;
                    name = supplierNames[i % supplierNames.length] + "有限公司";
                    code = supplierCodes[i];
                }
                
                String reason = reasons[i % reasons.length];
                
                try {
                    jdbcTemplate.update(insertSql,
                        supplierId, code, name, type, reason,
                        blacklistDate, isPermanent, expireDate, status, "测试数据"
                    );
                    inserted++;
                    log.debug("插入黑名单测试数据: {}", name);
                } catch (Exception e) {
                    log.warn("插入黑名单测试数据失败: {} - {}", name, e.getMessage());
                }
            }
            
            log.info("已插入{}条黑名单测试数据", inserted);
            
        } catch (Exception e) {
            log.error("插入黑名单测试数据失败", e);
        }
    }

    private void checkAndAddColumn(String tableName, String columnName, String columnDefinition) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, columnName);
            
            if (count == null || count == 0) {
                String alterSql = String.format("ALTER TABLE `%s` ADD COLUMN `%s` %s", tableName, columnName, columnDefinition);
                jdbcTemplate.execute(alterSql);
                log.info("添加字段成功: {}.{}", tableName, columnName);
            } else {
                log.debug("字段已存在: {}.{}", tableName, columnName);
            }
        } catch (Exception e) {
            log.error("检查或添加字段失败: {}.{}", tableName, columnName, e);
        }
    }

    private void insertTestData() {
        try {
            List<Long> supplierIds = insertSuppliers();
            if (supplierIds.isEmpty()) {
                log.warn("没有成功插入供应商数据");
                return;
            }
            
            insertQualifications(supplierIds);
            insertAlerts(supplierIds);
            insertClassificationLogs(supplierIds);
            verifyData();
            log.info("测试数据插入完成");
        } catch (Exception e) {
            log.error("插入测试数据失败", e);
        }
    }

    private List<Long> insertSuppliers() {
        List<Long> ids = new ArrayList<>();
        List<Map<String, Object>> suppliers = new ArrayList<>();
        
        Map<String, Object> s1 = new HashMap<>();
        s1.put("code", "SUP001");
        s1.put("name", "华东原材料供应商");
        s1.put("type", 1);
        s1.put("grade", 1);
        s1.put("material", 1);
        s1.put("cooperation", 1);
        s1.put("contact", "张三");
        s1.put("phone", "13800138001");
        s1.put("email", "zhangsan@example.com");
        s1.put("address", "上海市浦东新区");
        s1.put("remark", "优质原材料供应商");
        suppliers.add(s1);
        
        Map<String, Object> s2 = new HashMap<>();
        s2.put("code", "SUP002");
        s2.put("name", "华南辅料供应商");
        s2.put("type", 2);
        s2.put("grade", 2);
        s2.put("material", 2);
        s2.put("cooperation", 2);
        s2.put("contact", "李四");
        s2.put("phone", "13800138002");
        s2.put("email", "lisi@example.com");
        s2.put("address", "广州市天河区");
        s2.put("remark", "专业辅料供应商");
        suppliers.add(s2);
        
        Map<String, Object> s3 = new HashMap<>();
        s3.put("code", "SUP003");
        s3.put("name", "华北设备供应商");
        s3.put("type", 1);
        s3.put("grade", 3);
        s3.put("material", 3);
        s3.put("cooperation", 3);
        s3.put("contact", "王五");
        s3.put("phone", "13800138003");
        s3.put("email", "wangwu@example.com");
        s3.put("address", "北京市朝阳区");
        s3.put("remark", "高端设备供应商");
        suppliers.add(s3);
        
        Map<String, Object> s4 = new HashMap<>();
        s4.put("code", "SUP004");
        s4.put("name", "华中原材料供应商");
        s4.put("type", 1);
        s4.put("grade", 1);
        s4.put("material", 1);
        s4.put("cooperation", 2);
        s4.put("contact", "赵六");
        s4.put("phone", "13800138004");
        s4.put("email", "zhaoliu@example.com");
        s4.put("address", "武汉市洪山区");
        s4.put("remark", "战略级原材料供应商");
        suppliers.add(s4);
        
        Map<String, Object> s5 = new HashMap<>();
        s5.put("code", "SUP005");
        s5.put("name", "西南辅料供应商");
        s5.put("type", 2);
        s5.put("grade", 2);
        s5.put("material", 2);
        s5.put("cooperation", 1);
        s5.put("contact", "钱七");
        s5.put("phone", "13800138005");
        s5.put("email", "qianqi@example.com");
        s5.put("address", "成都市高新区");
        s5.put("remark", "重要辅料战略供应商");
        suppliers.add(s5);
        
        String sql = "INSERT INTO scm_supplier (supplier_code, supplier_name, supplier_type, grade, material_category, cooperation_level, contact_person, contact_phone, contact_email, address, status, remark, is_deleted, create_by, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, 0, 'admin', NOW(), NOW())";
        
        for (Map<String, Object> supplier : suppliers) {
            try {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, (String) supplier.get("code"));
                    ps.setString(2, (String) supplier.get("name"));
                    ps.setInt(3, (Integer) supplier.get("type"));
                    ps.setInt(4, (Integer) supplier.get("grade"));
                    ps.setInt(5, (Integer) supplier.get("material"));
                    ps.setInt(6, (Integer) supplier.get("cooperation"));
                    ps.setString(7, (String) supplier.get("contact"));
                    ps.setString(8, (String) supplier.get("phone"));
                    ps.setString(9, (String) supplier.get("email"));
                    ps.setString(10, (String) supplier.get("address"));
                    ps.setString(11, (String) supplier.get("remark"));
                    return ps;
                }, keyHolder);
                
                Long id = keyHolder.getKey().longValue();
                ids.add(id);
                log.info("插入供应商成功: {} -> ID={}", supplier.get("name"), id);
            } catch (Exception e) {
                log.error("插入供应商失败: {}", supplier.get("name"), e);
            }
        }
        
        log.info("已插入{}条供应商数据", ids.size());
        return ids;
    }

    private void insertQualifications(List<Long> supplierIds) {
        if (supplierIds.isEmpty()) {
            return;
        }
        
        String sql = "INSERT INTO scm_supplier_qualification (supplier_id, qualification_type, qualification_name, certificate_no, issuing_authority, issue_date, expiry_date, is_long_term, audit_status, alert_status, is_deleted, create_by, create_time, update_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin', NOW(), NOW(), ?)";
        
        int count = 0;
        
        for (int i = 0; i < supplierIds.size(); i++) {
            Long supplierId = supplierIds.get(i);
            
            try {
                int alertStatus;
                LocalDate expiryDate;
                String remark;
                
                if (i == 1) {
                    expiryDate = LocalDate.now().plusDays(30);
                    alertStatus = 1;
                    remark = "即将到期";
                } else if (i == 2) {
                    expiryDate = LocalDate.now().plusDays(45);
                    alertStatus = 1;
                    remark = "即将到期";
                } else if (i == 3) {
                    expiryDate = LocalDate.now().minusDays(15);
                    alertStatus = 2;
                    remark = "已过期";
                } else {
                    expiryDate = LocalDate.now().plusYears(2);
                    alertStatus = 0;
                    remark = "正常在有效期";
                }
                
                jdbcTemplate.update(sql, 
                    supplierId, "BUSINESS_LICENSE", "营业执照", 
                    "91310000X00" + (i+1), 
                    "工商行政管理局", 
                    LocalDate.now().minusYears(1), 
                    expiryDate, 
                    0, 2, alertStatus, remark);
                count++;
                
                if (i == 0 || i == 2) {
                    jdbcTemplate.update(sql, 
                        supplierId, "TAX_REGISTRATION", "税务登记证", 
                        "31000000X00" + (i+1), 
                        "税务局", 
                        LocalDate.now().minusYears(1), 
                        null, 
                        1, 2, 0, "长期有效");
                    count++;
                }
                
                if (i == 1) {
                    jdbcTemplate.update(sql, 
                        supplierId, "QUALITY_CERT", "ISO9001质量认证", 
                        "CNAS00" + (i+1), 
                        "中国质量认证中心", 
                        LocalDate.now().minusYears(1), 
                        LocalDate.now().plusYears(2), 
                        0, 2, 0, "有效期至2028年");
                    count++;
                }
                
                if (i == 2) {
                    jdbcTemplate.update(sql, 
                        supplierId, "PRODUCT_CERT", "产品3C认证", 
                        "CCC00" + (i+1), 
                        "中国质量认证中心", 
                        LocalDate.now().minusYears(1), 
                        null, 
                        1, 2, 0, "长期有效");
                    count++;
                }
                
            } catch (Exception e) {
                log.error("插入资质失败: 供应商ID={}", supplierId, e);
            }
        }
        
        log.info("已插入{}条资质数据", count);
    }

    private void insertAlerts(List<Long> supplierIds) {
        if (supplierIds.size() < 2) {
            return;
        }
        
        String sql = "INSERT INTO scm_qualification_alert (supplier_id, qualification_id, alert_type, alert_title, alert_content, days_before_expiry, is_read, alert_date, is_deleted, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin', NOW())";
        
        try {
            List<Map<String, Object>> qualifications = jdbcTemplate.queryForList(
                "SELECT id, supplier_id, alert_status FROM scm_supplier_qualification WHERE alert_status > 0 ORDER BY id");
            
            int count = 0;
            for (Map<String, Object> qual : qualifications) {
                Long qualId = ((Number) qual.get("id")).longValue();
                Long supplierId = ((Number) qual.get("supplier_id")).longValue();
                Integer alertStatus = ((Number) qual.get("alert_status")).intValue();
                
                if (alertStatus != null && alertStatus > 0) {
                    int alertType = alertStatus == 1 ? 1 : 2;
                    String title = alertStatus == 1 ? "资质即将到期提醒" : "资质已过期提醒";
                    String content = alertStatus == 1 
                        ? "供应商的资质将于30天后到期，请及时处理。" 
                        : "供应商的资质已过期，请立即处理。";
                    int days = alertStatus == 1 ? 30 : -15;
                    
                    jdbcTemplate.update(sql, supplierId, qualId, alertType, title, content, days, 0, LocalDate.now());
                    count++;
                }
            }
            
            if (!supplierIds.isEmpty()) {
                List<Map<String, Object>> normalQuals = jdbcTemplate.queryForList(
                    "SELECT id, supplier_id FROM scm_supplier_qualification WHERE alert_status = 0 LIMIT 1");
                for (Map<String, Object> qual : normalQuals) {
                    Long qualId = ((Number) qual.get("id")).longValue();
                    Long supplierId = ((Number) qual.get("supplier_id")).longValue();
                    
                    jdbcTemplate.update(sql, 
                        supplierId, 
                        qualId, 
                        0, "资质有效期正常", 
                        "供应商的资质有效期正常。", 
                        365, 1, 
                        LocalDate.now().minusDays(5));
                    count++;
                }
            }
            
            log.info("已插入{}条预警数据", count);
        } catch (Exception e) {
            log.error("插入预警数据失败", e);
        }
    }

    private void insertClassificationLogs(List<Long> supplierIds) {
        if (supplierIds.isEmpty()) {
            return;
        }
        
        String sql = "INSERT INTO scm_supplier_classification_log (supplier_id, old_material_category, new_material_category, old_cooperation_level, new_cooperation_level, change_reason, is_deleted, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, 0, 'admin', ?)";
        
        try {
            for (int i = 0; i < supplierIds.size(); i++) {
                Long supplierId = supplierIds.get(i);
                
                Map<String, Object> supplier = jdbcTemplate.queryForMap(
                    "SELECT material_category, cooperation_level FROM scm_supplier WHERE id = ?", 
                    supplierId);
                
                Integer material = (Integer) supplier.get("material_category");
                Integer cooperation = (Integer) supplier.get("cooperation_level");
                
                jdbcTemplate.update(sql, 
                    supplierId, null, material, null, cooperation, "初始分级", LocalDateTime.now());
                
                if (i < 3) {
                    LocalDateTime changeTime = LocalDateTime.now().minusDays(7 - i * 2);
                    
                    String[] reasons = new String[]{
                        "合作加深，升级为战略供应商",
                        "业务拓展，材料类别调整",
                        "合作关系调整"
                    };
                    
                    jdbcTemplate.update(sql, 
                        supplierId, material, 
                        material, 
                        cooperation, 
                        (cooperation % 3) + 1, 
                        reasons[i], 
                        changeTime);
                }
            }
            
            log.info("已插入分级分类记录");
        } catch (Exception e) {
            log.error("插入分级分类记录失败", e);
        }
    }

    private void verifyData() {
        try {
            Integer supplierCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scm_supplier WHERE is_deleted = 0", Integer.class);
            log.info("供应商数据: {}条", supplierCount);
            
            Integer qualificationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scm_supplier_qualification WHERE is_deleted = 0", Integer.class);
            log.info("资质数据: {}条", qualificationCount);
            
            Integer alertCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scm_qualification_alert WHERE is_deleted = 0", Integer.class);
            log.info("预警数据: {}条", alertCount);
            
            Integer logCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scm_supplier_classification_log WHERE is_deleted = 0", Integer.class);
            log.info("分级分类记录: {}条", logCount);
            
            try {
                String supplierName = jdbcTemplate.queryForObject(
                    "SELECT supplier_name FROM scm_supplier WHERE is_deleted = 0 ORDER BY id LIMIT 1", 
                    String.class);
                log.info("供应商名称验证: -> {}", supplierName);
            } catch (Exception e) {
                log.error("验证供应商名称失败", e);
            }
        } catch (Exception e) {
            log.error("验证数据失败", e);
        }
    }

    private void checkAndCreatePurchaseCollaborationTables() {
        try {
            checkAndCreatePurchaseRequestNewTable();
            checkAndCreatePurchaseRequestItemTable();
            checkAndCreateDemandSummaryTable();
            checkAndCreateDemandSummaryItemTable();
            checkAndCreateMaterialInventoryTable();
            checkAndCreateBomTable();
            checkAndCreateProductionWorkOrderTable();
            checkAndCreatePurchasePlanTable();
            checkAndCreatePurchasePlanItemTable();
            checkAndCreateOaApprovalTable();
            checkAndCreatePurchaseOrderTable();
            checkAndCreatePurchaseOrderItemTable();
            checkAndCreateOrderChangeTable();
            checkAndCreateProductionProgressTable();
            checkAndCreateShipmentTable();
            checkAndCreateLogisticsTrackTable();
            checkAndCreateDeliveryAppointmentTable();
            checkAndCreateIncomingInspectionTable();
            
            insertPurchaseCollaborationTestData();
            insertOrderExecutionTestData();
            
        } catch (Exception e) {
            log.error("检查或创建采购全流程协同表失败", e);
        }
    }

    private void checkAndCreatePurchaseRequestNewTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_purchase_request'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_purchase_request (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        req_no VARCHAR(50) COMMENT '申请单编号',
                        req_title VARCHAR(200) COMMENT '申请单标题',
                        req_dept VARCHAR(100) COMMENT '申请部门',
                        req_person VARCHAR(50) COMMENT '申请人',
                        req_phone VARCHAR(20) COMMENT '联系电话',
                        required_date DATE COMMENT '需求日期',
                        delivery_address VARCHAR(500) COMMENT '交货地址',
                        urgency TINYINT COMMENT '紧急程度：1普通 2紧急 3特急',
                        total_amount DECIMAL(18,2) COMMENT '总金额',
                        budget_source VARCHAR(100) COMMENT '预算来源',
                        description TEXT COMMENT '需求描述',
                        status TINYINT DEFAULT 0 COMMENT '状态：0草稿 1待提交 2审批中 3审批通过 4审批拒绝 5已转订单 6已取消',
                        approval_status VARCHAR(50) COMMENT '审批状态',
                        current_approver VARCHAR(50) COMMENT '当前审批人',
                        submit_time DATETIME COMMENT '提交时间',
                        approval_time DATETIME COMMENT '审批时间',
                        approval_remark VARCHAR(1000) COMMENT '审批备注',
                        generated_order_id BIGINT COMMENT '生成的采购订单ID',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_req_no (req_no),
                        KEY idx_status (status),
                        KEY idx_approval_status (approval_status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建采购申请表成功: scm_purchase_request");
            } else {
                log.debug("采购申请表已存在: scm_purchase_request");
            }
        } catch (Exception e) {
            log.error("检查或创建采购申请表失败", e);
        }
    }

    private void checkAndCreatePurchaseRequestItemTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_purchase_request_item'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_purchase_request_item (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        request_id BIGINT COMMENT '采购申请单ID',
                        material_code VARCHAR(50) COMMENT '物料编码',
                        material_name VARCHAR(200) COMMENT '物料名称',
                        material_spec VARCHAR(500) COMMENT '物料规格',
                        material_unit VARCHAR(20) COMMENT '物料单位',
                        material_category VARCHAR(100) COMMENT '物料类别',
                        quantity DECIMAL(18,2) COMMENT '需求数量',
                        unit_price DECIMAL(18,2) COMMENT '参考单价',
                        total_price DECIMAL(18,2) COMMENT '总金额',
                        remark VARCHAR(500) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_request_id (request_id),
                        KEY idx_material_code (material_code)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请明细表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建采购申请明细表成功: scm_purchase_request_item");
            } else {
                log.debug("采购申请明细表已存在: scm_purchase_request_item");
            }
        } catch (Exception e) {
            log.error("检查或创建采购申请明细表失败", e);
        }
    }

    private void checkAndCreateDemandSummaryTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_demand_summary'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_demand_summary (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        summary_no VARCHAR(50) COMMENT '汇总单编号',
                        summary_name VARCHAR(200) COMMENT '汇总单名称',
                        material_category VARCHAR(100) COMMENT '物料类别',
                        period_type TINYINT COMMENT '周期类型：1月度 2季度 3年度',
                        year INT COMMENT '年份',
                        month INT COMMENT '月份',
                        start_date DATE COMMENT '开始日期',
                        end_date DATE COMMENT '结束日期',
                        request_count INT COMMENT '关联申请单数量',
                        item_count INT COMMENT '物料种类数量',
                        total_quantity DECIMAL(18,2) COMMENT '总数量',
                        estimated_amount DECIMAL(18,2) COMMENT '预估金额',
                        status VARCHAR(50) COMMENT '状态',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_summary_no (summary_no),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求汇总表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建需求汇总表成功: scm_demand_summary");
            } else {
                log.debug("需求汇总表已存在: scm_demand_summary");
            }
        } catch (Exception e) {
            log.error("检查或创建需求汇总表失败", e);
        }
    }

    private void checkAndCreateDemandSummaryItemTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_demand_summary_item'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_demand_summary_item (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        summary_id BIGINT COMMENT '汇总单ID',
                        material_code VARCHAR(50) COMMENT '物料编码',
                        material_name VARCHAR(200) COMMENT '物料名称',
                        material_spec VARCHAR(500) COMMENT '物料规格',
                        material_unit VARCHAR(20) COMMENT '物料单位',
                        material_category VARCHAR(100) COMMENT '物料类别',
                        source_request_count INT COMMENT '来源申请单数量',
                        total_quantity DECIMAL(18,2) COMMENT '总数量',
                        avg_unit_price DECIMAL(18,2) COMMENT '平均单价',
                        estimated_amount DECIMAL(18,2) COMMENT '预估金额',
                        remark VARCHAR(500) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_summary_id (summary_id),
                        KEY idx_material_code (material_code)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求汇总明细表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建需求汇总明细表成功: scm_demand_summary_item");
            } else {
                log.debug("需求汇总明细表已存在: scm_demand_summary_item");
            }
        } catch (Exception e) {
            log.error("检查或创建需求汇总明细表失败", e);
        }
    }

    private void checkAndCreateMaterialInventoryTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_material_inventory'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_material_inventory (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        material_code VARCHAR(50) COMMENT '物料编码',
                        material_name VARCHAR(200) COMMENT '物料名称',
                        material_spec VARCHAR(500) COMMENT '物料规格',
                        material_unit VARCHAR(20) COMMENT '物料单位',
                        material_category VARCHAR(100) COMMENT '物料类别',
                        stock_quantity DECIMAL(18,2) COMMENT '库存数量',
                        available_quantity DECIMAL(18,2) COMMENT '可用数量',
                        reserved_quantity DECIMAL(18,2) COMMENT '预留数量',
                        safety_stock DECIMAL(18,2) COMMENT '安全库存',
                        minimum_stock DECIMAL(18,2) COMMENT '最低库存',
                        maximum_stock DECIMAL(18,2) COMMENT '最高库存',
                        warehouse_code VARCHAR(50) COMMENT '仓库编码',
                        warehouse_name VARCHAR(100) COMMENT '仓库名称',
                        location_code VARCHAR(50) COMMENT '库位编码',
                        unit_price DECIMAL(18,2) COMMENT '单价',
                        total_amount DECIMAL(18,2) COMMENT '总金额',
                        last_in_date DATE COMMENT '最近入库日期',
                        last_out_date DATE COMMENT '最近出库日期',
                        status TINYINT DEFAULT 1 COMMENT '状态',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_material_code (material_code),
                        KEY idx_warehouse_code (warehouse_code)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建库存表成功: scm_material_inventory");
            } else {
                log.debug("库存表已存在: scm_material_inventory");
            }
        } catch (Exception e) {
            log.error("检查或创建库存表失败", e);
        }
    }

    private void checkAndCreateBomTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_bom'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_bom (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        bom_no VARCHAR(50) COMMENT 'BOM编号',
                        bom_name VARCHAR(200) COMMENT 'BOM名称',
                        bom_version INT COMMENT 'BOM版本',
                        parent_code VARCHAR(50) COMMENT '父项编码',
                        parent_name VARCHAR(200) COMMENT '父项名称',
                        parent_spec VARCHAR(500) COMMENT '父项规格',
                        material_code VARCHAR(50) COMMENT '物料编码',
                        material_name VARCHAR(200) COMMENT '物料名称',
                        material_spec VARCHAR(500) COMMENT '物料规格',
                        material_unit VARCHAR(20) COMMENT '物料单位',
                        material_category VARCHAR(100) COMMENT '物料类别',
                        usage_quantity DECIMAL(18,4) COMMENT '用量',
                        scrap_rate DECIMAL(10,4) COMMENT '损耗率',
                        sort_order INT COMMENT '排序',
                        remark VARCHAR(500) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_bom_no (bom_no),
                        KEY idx_parent_code (parent_code),
                        KEY idx_material_code (material_code)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BOM表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建BOM表成功: scm_bom");
            } else {
                log.debug("BOM表已存在: scm_bom");
            }
        } catch (Exception e) {
            log.error("检查或创建BOM表失败", e);
        }
    }

    private void checkAndCreateProductionWorkOrderTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_production_work_order'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_production_work_order (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        work_order_no VARCHAR(50) COMMENT '工单编号',
                        work_order_name VARCHAR(200) COMMENT '工单名称',
                        product_code VARCHAR(50) COMMENT '产品编码',
                        product_name VARCHAR(200) COMMENT '产品名称',
                        product_spec VARCHAR(500) COMMENT '产品规格',
                        plan_quantity DECIMAL(18,2) COMMENT '计划数量',
                        actual_quantity DECIMAL(18,2) COMMENT '实际数量',
                        plan_start_date DATE COMMENT '计划开始日期',
                        plan_end_date DATE COMMENT '计划结束日期',
                        actual_start_date DATE COMMENT '实际开始日期',
                        actual_end_date DATE COMMENT '实际结束日期',
                        status TINYINT DEFAULT 0 COMMENT '状态：0待开始 1进行中 2已完成 3已取消',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_work_order_no (work_order_no),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产工单表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建生产工单表成功: scm_production_work_order");
            } else {
                log.debug("生产工单表已存在: scm_production_work_order");
            }
        } catch (Exception e) {
            log.error("检查或创建生产工单表失败", e);
        }
    }

    private void checkAndCreatePurchasePlanTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_purchase_plan'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_purchase_plan (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        plan_no VARCHAR(50) COMMENT '计划单编号',
                        plan_name VARCHAR(200) COMMENT '计划单名称',
                        plan_type TINYINT COMMENT '计划类型：1月度计划 2季度计划 3年度计划 4紧急计划 5补货计划',
                        source_type VARCHAR(50) COMMENT '来源类型：1需求汇总 2生产工单 3安全库存 4人工创建',
                        year INT COMMENT '年份',
                        month INT COMMENT '月份',
                        quarter INT COMMENT '季度',
                        start_date DATE COMMENT '开始日期',
                        end_date DATE COMMENT '结束日期',
                        item_count INT COMMENT '物料种类数量',
                        total_quantity DECIMAL(18,2) COMMENT '总数量',
                        estimated_amount DECIMAL(18,2) COMMENT '预估金额',
                        status VARCHAR(50) COMMENT '状态',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_plan_no (plan_no),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购计划表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建采购计划表成功: scm_purchase_plan");
            } else {
                log.debug("采购计划表已存在: scm_purchase_plan");
            }
        } catch (Exception e) {
            log.error("检查或创建采购计划表失败", e);
        }
    }

    private void checkAndCreatePurchasePlanItemTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_purchase_plan_item'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_purchase_plan_item (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        plan_id BIGINT COMMENT '采购计划ID',
                        material_code VARCHAR(50) COMMENT '物料编码',
                        material_name VARCHAR(200) COMMENT '物料名称',
                        material_spec VARCHAR(500) COMMENT '物料规格',
                        material_unit VARCHAR(20) COMMENT '物料单位',
                        material_category VARCHAR(100) COMMENT '物料类别',
                        required_quantity DECIMAL(18,2) COMMENT '需求数量',
                        stock_quantity DECIMAL(18,2) COMMENT '现有库存',
                        safety_stock DECIMAL(18,2) COMMENT '安全库存',
                        shortage_quantity DECIMAL(18,2) COMMENT '缺货数量',
                        purchase_quantity DECIMAL(18,2) COMMENT '建议采购数量',
                        unit_price DECIMAL(18,2) COMMENT '参考单价',
                        estimated_amount DECIMAL(18,2) COMMENT '预估金额',
                        recommended_supplier_id BIGINT COMMENT '推荐供应商ID',
                        recommended_supplier_name VARCHAR(200) COMMENT '推荐供应商名称',
                        remark VARCHAR(500) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_plan_id (plan_id),
                        KEY idx_material_code (material_code)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购计划明细表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建采购计划明细表成功: scm_purchase_plan_item");
            } else {
                log.debug("采购计划明细表已存在: scm_purchase_plan_item");
            }
        } catch (Exception e) {
            log.error("检查或创建采购计划明细表失败", e);
        }
    }

    private void checkAndCreateOaApprovalTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_oa_approval'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_oa_approval (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        approval_no VARCHAR(50) COMMENT '审批单编号',
                        source_type TINYINT COMMENT '来源类型：1采购申请 2采购计划 3采购订单',
                        source_id BIGINT COMMENT '来源单据ID',
                        source_no VARCHAR(50) COMMENT '来源单据编号',
                        approval_title VARCHAR(200) COMMENT '审批标题',
                        current_approver_id VARCHAR(50) COMMENT '当前审批人ID',
                        current_approver_name VARCHAR(50) COMMENT '当前审批人名称',
                        approval_status VARCHAR(50) COMMENT '审批状态',
                        submit_time DATETIME COMMENT '提交时间',
                        approval_time DATETIME COMMENT '审批时间',
                        approval_remark VARCHAR(1000) COMMENT '审批备注',
                        approval_history TEXT COMMENT '审批历史记录',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_approval_no (approval_no),
                        KEY idx_source (source_type, source_id),
                        KEY idx_approval_status (approval_status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OA审批记录表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建OA审批记录表成功: scm_oa_approval");
            } else {
                log.debug("OA审批记录表已存在: scm_oa_approval");
            }
        } catch (Exception e) {
            log.error("检查或创建OA审批记录表失败", e);
        }
    }

    private void checkAndCreatePurchaseOrderTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_purchase_order'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_purchase_order (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        order_no VARCHAR(50) COMMENT '订单编号',
                        order_name VARCHAR(200) COMMENT '订单名称',
                        supplier_id BIGINT COMMENT '供应商ID',
                        supplier_code VARCHAR(50) COMMENT '供应商编码',
                        supplier_name VARCHAR(200) COMMENT '供应商名称',
                        source_request_id BIGINT COMMENT '来源采购申请ID',
                        source_request_no VARCHAR(50) COMMENT '来源采购申请编号',
                        approval_id BIGINT COMMENT '审批单ID',
                        approval_no VARCHAR(50) COMMENT '审批单编号',
                        order_type TINYINT COMMENT '订单类型：1标准订单 2紧急订单 3补货订单',
                        order_date DATE COMMENT '订单日期',
                        expected_delivery_date DATE COMMENT '期望交货日期',
                        actual_delivery_date DATE COMMENT '实际交货日期',
                        item_count INT COMMENT '物料种类数量',
                        total_quantity DECIMAL(18,2) COMMENT '总数量',
                        total_amount DECIMAL(18,2) COMMENT '总金额',
                        payment_terms VARCHAR(500) COMMENT '付款条款',
                        delivery_terms VARCHAR(500) COMMENT '交货条款',
                        delivery_address VARCHAR(500) COMMENT '交货地址',
                        contact_person VARCHAR(50) COMMENT '联系人',
                        contact_phone VARCHAR(20) COMMENT '联系电话',
                        status TINYINT DEFAULT 0 COMMENT '状态：0新建 1待审批 2审批通过 3已发布 4部分收货 5全部收货 6已完成 7已取消',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_order_no (order_no),
                        KEY idx_supplier_id (supplier_id),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建采购订单表成功: scm_purchase_order");
            } else {
                log.debug("采购订单表已存在: scm_purchase_order");
            }
        } catch (Exception e) {
            log.error("检查或创建采购订单表失败", e);
        }
    }

    private void insertPurchaseCollaborationTestData() {
        try {
            jdbcTemplate.execute("SET NAMES utf8mb4");
            jdbcTemplate.execute("SET CHARACTER SET utf8mb4");
            
            String requestCountSql = "SELECT COUNT(*) FROM scm_purchase_request WHERE is_deleted = 0";
            Integer requestCount = jdbcTemplate.queryForObject(requestCountSql, Integer.class);
            
            if (requestCount == null || requestCount < 30) {
                insertPurchaseRequestTestData();
            } else {
                log.info("采购申请测试数据已存在: {}条", requestCount);
            }
            
            String inventoryCountSql = "SELECT COUNT(*) FROM scm_material_inventory WHERE is_deleted = 0";
            Integer inventoryCount = jdbcTemplate.queryForObject(inventoryCountSql, Integer.class);
            
            if (inventoryCount == null || inventoryCount < 30) {
                insertInventoryTestData();
            } else {
                log.info("库存测试数据已存在: {}条", inventoryCount);
            }
            
            String bomCountSql = "SELECT COUNT(*) FROM scm_bom WHERE is_deleted = 0";
            Integer bomCount = jdbcTemplate.queryForObject(bomCountSql, Integer.class);
            
            if (bomCount == null || bomCount < 20) {
                insertBomTestData();
            } else {
                log.info("BOM测试数据已存在: {}条", bomCount);
            }
            
            String workOrderCountSql = "SELECT COUNT(*) FROM scm_production_work_order WHERE is_deleted = 0";
            Integer workOrderCount = jdbcTemplate.queryForObject(workOrderCountSql, Integer.class);
            
            if (workOrderCount == null || workOrderCount < 20) {
                insertWorkOrderTestData();
            } else {
                log.info("生产工单测试数据已存在: {}条", workOrderCount);
            }
            
            String requestItemCountSql = "SELECT COUNT(*) FROM scm_purchase_request_item WHERE is_deleted = 0";
            Integer requestItemCount = jdbcTemplate.queryForObject(requestItemCountSql, Integer.class);
            
            if (requestItemCount == null || requestItemCount < 50) {
                insertPurchaseRequestItemTestData();
            } else {
                log.info("采购申请明细测试数据已存在: {}条", requestItemCount);
            }
            
            String summaryCountSql = "SELECT COUNT(*) FROM scm_demand_summary WHERE is_deleted = 0";
            Integer summaryCount = jdbcTemplate.queryForObject(summaryCountSql, Integer.class);
            
            if (summaryCount == null || summaryCount < 30) {
                insertDemandSummaryTestData();
            } else {
                log.info("需求汇总测试数据已存在: {}条", summaryCount);
            }
            
            String planCountSql = "SELECT COUNT(*) FROM scm_purchase_plan WHERE is_deleted = 0";
            Integer planCount = jdbcTemplate.queryForObject(planCountSql, Integer.class);
            
            if (planCount == null || planCount < 30) {
                insertPurchasePlanTestData();
            } else {
                log.info("采购计划测试数据已存在: {}条", planCount);
            }
            
            String approvalCountSql = "SELECT COUNT(*) FROM scm_oa_approval WHERE is_deleted = 0";
            Integer approvalCount = jdbcTemplate.queryForObject(approvalCountSql, Integer.class);
            
            if (approvalCount == null || approvalCount < 30) {
                insertApprovalTestData();
            } else {
                log.info("审批联动测试数据已存在: {}条", approvalCount);
            }
            
        } catch (Exception e) {
            log.error("插入采购全流程协同测试数据失败", e);
        }
    }

    private void insertPurchaseRequestItemTestData() {
        try {
            List<Map<String, Object>> requests = jdbcTemplate.queryForList(
                "SELECT id, req_no, req_title FROM scm_purchase_request WHERE is_deleted = 0 ORDER BY id"
            );
            
            if (requests.isEmpty()) {
                log.warn("没有采购申请数据，无法生成采购申请明细测试数据");
                return;
            }
            
            String[] materialNames = {
                "不锈钢板", "铝型材", "铜排", "塑料颗粒", "橡胶密封条",
                "电子元器件", "集成电路芯片", "电阻电容", "连接器", "电缆线",
                "液压油", "润滑油", "冷却液", "清洗剂", "防锈剂",
                "轴承", "齿轮", "密封圈", "紧固件", "弹簧",
                "电机", "传感器", "控制器", "显示屏", "开关按钮"
            };
            
            String[] specs = {"304", "6061", "T2", "PP", "EPDM", "SMT", "DIP", "SMD", "RJ45", "RVV"};
            String[] units = {"张", "件", "米", "千克", "个", "套", "台", "卷", "箱", "包"};
            String[] categories = {"1", "2", "3"};
            
            String insertSql = """
                INSERT INTO scm_purchase_request_item 
                (request_id, material_code, material_name, material_spec, material_unit, material_category,
                 quantity, unit_price, total_price, remark, is_deleted, create_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
                """;
            
            int inserted = 0;
            
            for (int i = 0; i < requests.size(); i++) {
                Map<String, Object> request = requests.get(i);
                Long requestId = ((Number) request.get("id")).longValue();
                
                int itemCount = 2 + (i % 4);
                
                for (int j = 0; j < itemCount; j++) {
                    int materialIndex = (i * 3 + j) % materialNames.length;
                    String materialName = materialNames[materialIndex];
                    String materialCode = "MAT" + String.format("%03d", materialIndex + 1);
                    String spec = specs[materialIndex % specs.length];
                    String unit = units[materialIndex % units.length];
                    String category = categories[materialIndex % categories.length];
                    
                    java.math.BigDecimal quantity = new java.math.BigDecimal(10 + materialIndex * 5);
                    java.math.BigDecimal unitPrice = new java.math.BigDecimal(100 + materialIndex * 50);
                    java.math.BigDecimal totalPrice = quantity.multiply(unitPrice);
                    String remark = "采购申请明细测试数据" + (inserted + 1);
                    
                    try {
                        jdbcTemplate.update(insertSql,
                            requestId, materialCode, materialName, spec, unit, category,
                            quantity, unitPrice, totalPrice, remark
                        );
                        inserted++;
                    } catch (Exception e) {
                        log.warn("插入采购申请明细测试数据失败: {} - {}", requestId, e.getMessage());
                    }
                }
            }
            
            log.info("已插入{}条采购申请明细测试数据", inserted);
            
        } catch (Exception e) {
            log.error("插入采购申请明细测试数据失败", e);
        }
    }

    private void insertDemandSummaryTestData() {
        LocalDate today = LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        
        String[] materialNames = {
            "不锈钢板", "铝型材", "铜排", "塑料颗粒", "橡胶密封条",
            "电子元器件", "集成电路芯片", "电阻电容", "连接器", "电缆线",
            "液压油", "润滑油", "冷却液", "清洗剂", "防锈剂",
            "轴承", "齿轮", "密封圈", "紧固件", "弹簧",
            "电机", "传感器", "控制器", "显示屏", "开关按钮"
        };
        
        String[] specs = {"304", "6061", "T2", "PP", "EPDM", "SMT", "DIP", "SMD", "RJ45", "RVV"};
        String[] units = {"张", "件", "米", "千克", "个", "套", "台", "卷", "箱", "包"};
        String[] categories = {"1", "2", "3"};
        String[] statuses = {"DRAFT", "CONFIRMED", "PROCESSED"};
        
        String insertSummarySql = """
            INSERT INTO scm_demand_summary 
            (summary_no, summary_name, material_category, period_type, year, month,
             start_date, end_date, request_count, item_count, total_quantity, estimated_amount,
             status, remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        String insertItemSql = """
            INSERT INTO scm_demand_summary_item 
            (summary_id, material_code, material_name, material_spec, material_unit, material_category,
             source_request_count, total_quantity, avg_unit_price, estimated_amount, remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        int summaryInserted = 0;
        int itemInserted = 0;
        
        for (int i = 0; i < 35; i++) {
            String summaryNo = "DS" + today.minusDays(i).format(formatter) + String.format("%05d", i + 1);
            int periodType = (i % 3) + 1;
            int year = today.getYear() - (i / 12);
            int month = (i % 12) + 1;
            
            LocalDate startDate;
            LocalDate endDate;
            
            if (periodType == 1) {
                startDate = LocalDate.of(year, month, 1);
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            } else if (periodType == 2) {
                int startMonth = ((month - 1) / 3) * 3 + 1;
                startDate = LocalDate.of(year, startMonth, 1);
                endDate = startDate.plusMonths(3).minusDays(1);
            } else {
                startDate = LocalDate.of(year, 1, 1);
                endDate = LocalDate.of(year, 12, 31);
            }
            
            int requestCount = 3 + (i % 5);
            int itemCount = 2 + (i % 4);
            String status = statuses[i % statuses.length];
            String category = categories[i % categories.length];
            
            java.math.BigDecimal totalQuantity = java.math.BigDecimal.ZERO;
            java.math.BigDecimal estimatedAmount = java.math.BigDecimal.ZERO;
            
            List<Map<String, Object>> items = new ArrayList<>();
            
            for (int j = 0; j < itemCount; j++) {
                int materialIndex = (i * 3 + j) % materialNames.length;
                String materialName = materialNames[materialIndex];
                String materialCode = "MAT" + String.format("%03d", materialIndex + 1);
                String spec = specs[materialIndex % specs.length];
                String unit = units[materialIndex % units.length];
                String matCategory = categories[materialIndex % categories.length];
                
                int sourceCount = 1 + (j % 3);
                java.math.BigDecimal qty = new java.math.BigDecimal(50 + materialIndex * 20);
                java.math.BigDecimal avgPrice = new java.math.BigDecimal(100 + materialIndex * 50);
                java.math.BigDecimal estAmount = qty.multiply(avgPrice);
                
                totalQuantity = totalQuantity.add(qty);
                estimatedAmount = estimatedAmount.add(estAmount);
                
                Map<String, Object> item = new HashMap<>();
                item.put("materialCode", materialCode);
                item.put("materialName", materialName);
                item.put("spec", spec);
                item.put("unit", unit);
                item.put("category", matCategory);
                item.put("sourceCount", sourceCount);
                item.put("qty", qty);
                item.put("avgPrice", avgPrice);
                item.put("estAmount", estAmount);
                items.add(item);
            }
            
            String summaryName = "需求汇总-" + year + "年" + (periodType == 1 ? month + "月" : periodType == 2 ? "第" + ((month - 1) / 3 + 1) + "季度" : "年度");
            String remark = "需求汇总测试数据" + (i + 1);
            
            try {
                jdbcTemplate.update(insertSummarySql,
                    summaryNo, summaryName, category, periodType, year, month,
                    startDate, endDate, requestCount, itemCount, totalQuantity, estimatedAmount,
                    status, remark
                );
                summaryInserted++;
                
                List<Map<String, Object>> summaryList = jdbcTemplate.queryForList(
                    "SELECT id FROM scm_demand_summary WHERE summary_no = ?", summaryNo
                );
                if (!summaryList.isEmpty()) {
                    Long summaryId = ((Number) summaryList.get(0).get("id")).longValue();
                    
                    for (Map<String, Object> item : items) {
                        try {
                            jdbcTemplate.update(insertItemSql,
                                summaryId, 
                                item.get("materialCode"), item.get("materialName"), item.get("spec"), 
                                item.get("unit"), item.get("category"),
                                item.get("sourceCount"), item.get("qty"), item.get("avgPrice"), item.get("estAmount"),
                                "需求汇总明细测试数据" + (itemInserted + 1)
                            );
                            itemInserted++;
                        } catch (Exception e) {
                            log.warn("插入需求汇总明细测试数据失败: {} - {}", summaryId, e.getMessage());
                        }
                    }
                }
                
            } catch (Exception e) {
                log.warn("插入需求汇总测试数据失败: {} - {}", summaryNo, e.getMessage());
            }
        }
        
        log.info("已插入{}条需求汇总测试数据，{}条明细数据", summaryInserted, itemInserted);
    }

    private void insertPurchasePlanTestData() {
        LocalDate today = LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        
        String[] materialNames = {
            "不锈钢板", "铝型材", "铜排", "塑料颗粒", "橡胶密封条",
            "电子元器件", "集成电路芯片", "电阻电容", "连接器", "电缆线",
            "液压油", "润滑油", "冷却液", "清洗剂", "防锈剂",
            "轴承", "齿轮", "密封圈", "紧固件", "弹簧",
            "电机", "传感器", "控制器", "显示屏", "开关按钮"
        };
        
        String[] specs = {"304", "6061", "T2", "PP", "EPDM", "SMT", "DIP", "SMD", "RJ45", "RVV"};
        String[] units = {"张", "件", "米", "千克", "个", "套", "台", "卷", "箱", "包"};
        String[] categories = {"1", "2", "3"};
        String[] sourceTypes = {"DEMAND_SUMMARY", "WORK_ORDER", "SAFETY_STOCK", "MANUAL", "BOTH"};
        String[] statuses = {"DRAFT", "SUBMITTED", "APPROVED", "EXECUTING", "COMPLETED", "CANCELLED"};
        
        String insertPlanSql = """
            INSERT INTO scm_purchase_plan 
            (plan_no, plan_name, plan_type, source_type, year, month, quarter,
             start_date, end_date, item_count, total_quantity, estimated_amount,
             status, remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        String insertItemSql = """
            INSERT INTO scm_purchase_plan_item 
            (plan_id, material_code, material_name, material_spec, material_unit, material_category,
             required_quantity, stock_quantity, safety_stock, shortage_quantity, purchase_quantity,
             unit_price, estimated_amount, recommended_supplier_id, recommended_supplier_name,
             remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        int planInserted = 0;
        int itemInserted = 0;
        
        for (int i = 0; i < 35; i++) {
            String planNo = "PP" + today.minusDays(i).format(formatter) + String.format("%05d", i + 1);
            int planType = (i % 5) + 1;
            String sourceType = sourceTypes[i % sourceTypes.length];
            int year = today.getYear() - (i / 12);
            int month = (i % 12) + 1;
            int quarter = ((month - 1) / 3) + 1;
            
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            
            int itemCount = 2 + (i % 4);
            String status = statuses[i % statuses.length];
            
            java.math.BigDecimal totalQuantity = java.math.BigDecimal.ZERO;
            java.math.BigDecimal estimatedAmount = java.math.BigDecimal.ZERO;
            
            List<Map<String, Object>> items = new ArrayList<>();
            
            for (int j = 0; j < itemCount; j++) {
                int materialIndex = (i * 3 + j) % materialNames.length;
                String materialName = materialNames[materialIndex];
                String materialCode = "MAT" + String.format("%03d", materialIndex + 1);
                String spec = specs[materialIndex % specs.length];
                String unit = units[materialIndex % units.length];
                String category = categories[materialIndex % categories.length];
                
                java.math.BigDecimal requiredQty = new java.math.BigDecimal(100 + materialIndex * 50);
                java.math.BigDecimal stockQty = new java.math.BigDecimal(20 + materialIndex * 10);
                java.math.BigDecimal safetyStock = new java.math.BigDecimal(50 + materialIndex * 20);
                java.math.BigDecimal shortageQty = requiredQty.add(safetyStock).subtract(stockQty);
                if (shortageQty.compareTo(java.math.BigDecimal.ZERO) < 0) {
                    shortageQty = java.math.BigDecimal.ZERO;
                }
                java.math.BigDecimal purchaseQty = shortageQty;
                java.math.BigDecimal unitPrice = new java.math.BigDecimal(100 + materialIndex * 50);
                java.math.BigDecimal estAmount = purchaseQty.multiply(unitPrice);
                
                totalQuantity = totalQuantity.add(purchaseQty);
                estimatedAmount = estimatedAmount.add(estAmount);
                
                Map<String, Object> item = new HashMap<>();
                item.put("materialCode", materialCode);
                item.put("materialName", materialName);
                item.put("spec", spec);
                item.put("unit", unit);
                item.put("category", category);
                item.put("requiredQty", requiredQty);
                item.put("stockQty", stockQty);
                item.put("safetyStock", safetyStock);
                item.put("shortageQty", shortageQty);
                item.put("purchaseQty", purchaseQty);
                item.put("unitPrice", unitPrice);
                item.put("estAmount", estAmount);
                item.put("supplierId", (long) (materialIndex % 3 + 1));
                item.put("supplierName", "供应商" + (materialIndex % 3 + 1));
                items.add(item);
            }
            
            String planTypeName = "";
            switch (planType) {
                case 1: planTypeName = "月度计划"; break;
                case 2: planTypeName = "季度计划"; break;
                case 3: planTypeName = "年度计划"; break;
                case 4: planTypeName = "紧急计划"; break;
                case 5: planTypeName = "补货计划"; break;
            }
            String planName = planTypeName + "-" + year + "年" + month + "月";
            String remark = "采购计划测试数据" + (i + 1);
            
            try {
                jdbcTemplate.update(insertPlanSql,
                    planNo, planName, planType, sourceType, year, month, quarter,
                    startDate, endDate, itemCount, totalQuantity, estimatedAmount,
                    status, remark
                );
                planInserted++;
                
                List<Map<String, Object>> planList = jdbcTemplate.queryForList(
                    "SELECT id FROM scm_purchase_plan WHERE plan_no = ?", planNo
                );
                if (!planList.isEmpty()) {
                    Long planId = ((Number) planList.get(0).get("id")).longValue();
                    
                    for (Map<String, Object> item : items) {
                        try {
                            jdbcTemplate.update(insertItemSql,
                                planId,
                                item.get("materialCode"), item.get("materialName"), item.get("spec"),
                                item.get("unit"), item.get("category"),
                                item.get("requiredQty"), item.get("stockQty"), item.get("safetyStock"),
                                item.get("shortageQty"), item.get("purchaseQty"),
                                item.get("unitPrice"), item.get("estAmount"),
                                item.get("supplierId"), item.get("supplierName"),
                                "采购计划明细测试数据" + (itemInserted + 1)
                            );
                            itemInserted++;
                        } catch (Exception e) {
                            log.warn("插入采购计划明细测试数据失败: {} - {}", planId, e.getMessage());
                        }
                    }
                }
                
            } catch (Exception e) {
                log.warn("插入采购计划测试数据失败: {} - {}", planNo, e.getMessage());
            }
        }
        
        log.info("已插入{}条采购计划测试数据，{}条明细数据", planInserted, itemInserted);
    }

    private void insertApprovalTestData() {
        LocalDate today = LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        
        String[] approvalStatuses = {"DRAFT", "APPROVING", "APPROVED", "REJECTED", "WITHDRAWN"};
        String[] approvers = {"审批专员", "财务主管", "采购经理", "总经理", "副总"};
        
        String insertSql = """
            INSERT INTO scm_oa_approval 
            (approval_no, source_type, source_id, source_no, approval_title,
             current_approver_id, current_approver_name, approval_status, submit_time,
             approval_time, approval_remark, approval_history, remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        int inserted = 0;
        
        for (int i = 0; i < 40; i++) {
            String approvalNo = "OA" + today.minusDays(i).format(formatter) + String.format("%05d", i + 1);
            int sourceType = (i % 3) + 1;
            Long sourceId = (long) (i + 1);
            String sourceNo = "";
            
            switch (sourceType) {
                case 1: sourceNo = "PR" + today.minusDays(i).format(formatter) + String.format("%05d", i + 1); break;
                case 2: sourceNo = "PP" + today.minusDays(i).format(formatter) + String.format("%05d", i + 1); break;
                case 3: sourceNo = "PO" + today.minusDays(i).format(formatter) + String.format("%05d", i + 1); break;
            }
            
            String sourceTypeName = "";
            switch (sourceType) {
                case 1: sourceTypeName = "采购申请"; break;
                case 2: sourceTypeName = "采购计划"; break;
                case 3: sourceTypeName = "采购订单"; break;
            }
            
            String approvalTitle = sourceTypeName + "审批-" + sourceNo;
            String approvalStatus = approvalStatuses[i % approvalStatuses.length];
            String approverName = approvers[i % approvers.length];
            
            LocalDateTime submitTime = today.minusDays(i).atTime(10, 0);
            LocalDateTime approvalTime = null;
            String approvalRemark = "";
            String approvalHistory = "";
            
            if ("APPROVED".equals(approvalStatus) || "REJECTED".equals(approvalStatus)) {
                approvalTime = submitTime.plusHours(2 + i % 8);
                approvalRemark = "APPROVED".equals(approvalStatus) ? "审批通过" : "审批拒绝，请重新提交";
                
                Map<String, Object> historyNode = new HashMap<>();
                historyNode.put("operator", approverName);
                historyNode.put("action", "APPROVED".equals(approvalStatus) ? "APPROVE" : "REJECT");
                historyNode.put("remark", approvalRemark);
                historyNode.put("time", approvalTime.toString());
                approvalHistory = historyNode.toString();
            }
            
            String remark = "审批联动测试数据" + (i + 1);
            
            try {
                jdbcTemplate.update(insertSql,
                    approvalNo, sourceType, sourceId, sourceNo, approvalTitle,
                    "1", approverName, approvalStatus, submitTime,
                    approvalTime, approvalRemark, approvalHistory, remark
                );
                inserted++;
            } catch (Exception e) {
                log.warn("插入审批联动测试数据失败: {} - {}", approvalNo, e.getMessage());
            }
        }
        
        log.info("已插入{}条审批联动测试数据", inserted);
    }

    private void insertPurchaseRequestTestData() {
        LocalDate today = LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        
        String[] depts = {"生产部", "技术部", "采购部", "质量部", "设备部", "仓库", "行政部", "财务部", "销售部", "研发部"};
        String[] persons = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十", "郑十一", "王十二"};
        String[] phones = {"13800138001", "13800138002", "13800138003", "13800138004", "13800138005"};
        
        String[] materialNames = {
            "不锈钢板", "铝型材", "铜排", "塑料颗粒", "橡胶密封条",
            "电子元器件", "集成电路芯片", "电阻电容", "连接器", "电缆线",
            "液压油", "润滑油", "冷却液", "清洗剂", "防锈剂",
            "轴承", "齿轮", "密封圈", "紧固件", "弹簧",
            "电机", "传感器", "控制器", "显示屏", "开关按钮",
            "包装材料", "标签", "纸箱", "托盘", "缓冲材料",
            "工具量具", "劳保用品", "办公用品", "清洁用品", "消防器材"
        };
        
        String[] specs = {"304", "6061", "T2", "PP", "EPDM", "SMT", "DIP", "SMD", "RJ45", "RVV"};
        String[] units = {"张", "件", "米", "千克", "个", "套", "台", "卷", "箱", "包"};
        
        String insertRequestSql = """
            INSERT INTO scm_purchase_request 
            (req_no, req_title, req_dept, req_person, req_phone, required_date, 
             delivery_address, urgency, total_amount, budget_source, description, 
             status, approval_status, remark, is_deleted, create_by, create_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin', ?)
            """;
        
        int inserted = 0;
        
        for (int i = 0; i < 40; i++) {
            LocalDate createDate = today.minusDays(i);
            String reqNo = "PR" + createDate.format(formatter) + String.format("%05d", i + 1);
            String materialName = materialNames[i % materialNames.length];
            String reqTitle = "采购申请 - " + materialName + (i % 5 == 0 ? "（紧急）" : "");
            String dept = depts[i % depts.length];
            String person = persons[i % persons.length];
            String phone = phones[i % phones.length];
            LocalDate requiredDate = today.plusDays(7 + i);
            String address = "某某市某某区某某工业园区A栋" + (i % 5 + 1) + "号仓库";
            int urgency = (i % 3) + 1;
            java.math.BigDecimal totalAmount = new java.math.BigDecimal(1000 + i * 500 + (int)(Math.random() * 10000));
            String budgetSource = i % 2 == 0 ? "年度预算" : "紧急采购预算";
            String description = "采购" + materialName + "，用于生产线维护和产品生产。";
            
            int status;
            String approvalStatus;
            if (i % 3 == 0) {
                status = 2;
                approvalStatus = "APPROVING";
            } else if (i % 3 == 1) {
                status = 3;
                approvalStatus = "APPROVED";
            } else {
                status = i % 6;
                approvalStatus = "DRAFT";
                if (status == 2) approvalStatus = "APPROVING";
                else if (status == 3) approvalStatus = "APPROVED";
                else if (status == 4) approvalStatus = "REJECTED";
                else if (status == 5) approvalStatus = "APPROVED";
            }
            
            String remark = "测试数据" + (i + 1);
            java.sql.Timestamp createTime = java.sql.Timestamp.valueOf(createDate.atTime(java.time.LocalTime.of(9 + (i % 8), i % 60)));
            
            try {
                jdbcTemplate.update(insertRequestSql,
                    reqNo, reqTitle, dept, person, phone, requiredDate,
                    address, urgency, totalAmount, budgetSource, description,
                    status, approvalStatus, remark, createTime
                );
                inserted++;
            } catch (Exception e) {
                log.warn("插入采购申请测试数据失败: {} - {}", reqNo, e.getMessage());
            }
        }
        
        log.info("已插入{}条采购申请测试数据", inserted);
    }

    private void insertInventoryTestData() {
        LocalDate today = LocalDate.now();
        
        String[] materialCodes = {
            "MAT001", "MAT002", "MAT003", "MAT004", "MAT005",
            "MAT006", "MAT007", "MAT008", "MAT009", "MAT010",
            "MAT011", "MAT012", "MAT013", "MAT014", "MAT015",
            "MAT016", "MAT017", "MAT018", "MAT019", "MAT020",
            "MAT021", "MAT022", "MAT023", "MAT024", "MAT025",
            "MAT026", "MAT027", "MAT028", "MAT029", "MAT030",
            "MAT031", "MAT032", "MAT033", "MAT034", "MAT035"
        };
        
        String[] materialNames = {
            "不锈钢板", "铝型材", "铜排", "塑料颗粒", "橡胶密封条",
            "电子元器件", "集成电路芯片", "电阻电容", "连接器", "电缆线",
            "液压油", "润滑油", "冷却液", "清洗剂", "防锈剂",
            "轴承", "齿轮", "密封圈", "紧固件", "弹簧",
            "电机", "传感器", "控制器", "显示屏", "开关按钮",
            "包装材料", "标签", "纸箱", "托盘", "缓冲材料",
            "工具量具", "劳保用品", "办公用品", "清洁用品", "消防器材"
        };
        
        String[] warehouses = {"原料仓库", "成品仓库", "备品备件库", "危险品库", "周转仓库"};
        
        String insertSql = """
            INSERT INTO scm_material_inventory 
            (material_code, material_name, material_spec, material_unit, material_category,
             stock_quantity, available_quantity, reserved_quantity, safety_stock, minimum_stock, maximum_stock,
             warehouse_code, warehouse_name, location_code, unit_price, total_amount,
             last_in_date, last_out_date, status, remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, 0, 'admin')
            """;
        
        int inserted = 0;
        
        for (int i = 0; i < 35; i++) {
            String code = materialCodes[i];
            String name = materialNames[i];
            String spec = "规格" + (i % 10 + 1);
            String unit = i % 3 == 0 ? "千克" : (i % 3 == 1 ? "件" : "米");
            String category = String.valueOf((i % 3) + 1);
            
            java.math.BigDecimal stockQty = new java.math.BigDecimal(100 + i * 50 + (int)(Math.random() * 500));
            java.math.BigDecimal availableQty = stockQty.multiply(new java.math.BigDecimal("0.8")).setScale(2, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal reservedQty = stockQty.subtract(availableQty);
            java.math.BigDecimal safetyStock = new java.math.BigDecimal(50 + i * 10);
            java.math.BigDecimal minStock = safetyStock.multiply(new java.math.BigDecimal("0.5"));
            java.math.BigDecimal maxStock = safetyStock.multiply(new java.math.BigDecimal("3"));
            
            String warehouse = warehouses[i % warehouses.length];
            String location = "A-" + String.format("%02d", i / 5 + 1) + "-" + String.format("%02d", i % 10 + 1);
            java.math.BigDecimal unitPrice = new java.math.BigDecimal(10 + i * 5 + (int)(Math.random() * 100));
            java.math.BigDecimal totalAmount = stockQty.multiply(unitPrice);
            
            LocalDate lastInDate = today.minusDays(i % 30);
            LocalDate lastOutDate = today.minusDays((i + 5) % 20);
            String remark = "库存测试数据" + (i + 1);
            
            try {
                jdbcTemplate.update(insertSql,
                    code, name, spec, unit, category,
                    stockQty, availableQty, reservedQty, safetyStock, minStock, maxStock,
                    "WH" + (i % warehouses.length + 1), warehouse, location, unitPrice, totalAmount,
                    lastInDate, lastOutDate, remark
                );
                inserted++;
            } catch (Exception e) {
                log.warn("插入库存测试数据失败: {} - {}", code, e.getMessage());
            }
        }
        
        log.info("已插入{}条库存测试数据", inserted);
    }

    private void insertBomTestData() {
        String[] bomNos = {"BOM001", "BOM002", "BOM003", "BOM004", "BOM005"};
        String[] productNames = {"智能控制器", "数据采集终端", "电源模块", "通讯网关", "传感器组件"};
        
        String[] materialCodes = {
            "MAT006", "MAT007", "MAT008", "MAT009", "MAT010",
            "MAT016", "MAT017", "MAT018", "MAT019", "MAT020",
            "MAT021", "MAT022", "MAT023", "MAT024", "MAT025"
        };
        
        String[] materialNames = {
            "电子元器件", "集成电路芯片", "电阻电容", "连接器", "电缆线",
            "轴承", "齿轮", "密封圈", "紧固件", "弹簧",
            "电机", "传感器", "控制器", "显示屏", "开关按钮"
        };
        
        String insertSql = """
            INSERT INTO scm_bom 
            (bom_no, bom_name, bom_version, parent_code, parent_name, parent_spec,
             material_code, material_name, material_spec, material_unit, material_category,
             usage_quantity, scrap_rate, sort_order, remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        int inserted = 0;
        
        for (int i = 0; i < 5; i++) {
            String bomNo = bomNos[i];
            String productName = productNames[i];
            String parentCode = "PROD" + String.format("%03d", i + 1);
            
            for (int j = 0; j < 5; j++) {
                int materialIndex = i * 3 + j;
                if (materialIndex >= materialCodes.length) materialIndex = materialIndex % materialCodes.length;
                
                String materialCode = materialCodes[materialIndex];
                String materialName = materialNames[materialIndex];
                java.math.BigDecimal usageQty = new java.math.BigDecimal(1 + j).setScale(2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal scrapRate = new java.math.BigDecimal("0.0" + (j + 1));
                
                try {
                    jdbcTemplate.update(insertSql,
                        bomNo, productName + " BOM", 1, parentCode, productName, "V1.0",
                        materialCode, materialName, "规格" + (materialIndex % 10 + 1), 
                        j % 2 == 0 ? "个" : "件", String.valueOf((materialIndex % 3) + 1),
                        usageQty, scrapRate, j + 1, "BOM测试数据" + (inserted + 1)
                    );
                    inserted++;
                } catch (Exception e) {
                    log.warn("插入BOM测试数据失败: {} - {}", bomNo, e.getMessage());
                }
            }
        }
        
        log.info("已插入{}条BOM测试数据", inserted);
    }

    private void insertWorkOrderTestData() {
        LocalDate today = LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        
        String[] productNames = {"智能控制器", "数据采集终端", "电源模块", "通讯网关", "传感器组件"};
        String[] productCodes = {"PROD001", "PROD002", "PROD003", "PROD004", "PROD005"};
        
        String insertSql = """
            INSERT INTO scm_production_work_order 
            (work_order_no, work_order_name, product_code, product_name, product_spec,
             plan_quantity, actual_quantity, plan_start_date, plan_end_date,
             actual_start_date, actual_end_date, status, remark, is_deleted, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
            """;
        
        int inserted = 0;
        
        for (int i = 0; i < 25; i++) {
            String workOrderNo = "WO" + today.minusDays(i).format(formatter) + String.format("%04d", i + 1);
            int productIndex = i % 5;
            String productName = productNames[productIndex];
            String productCode = productCodes[productIndex];
            String workOrderName = productName + "生产工单" + (i + 1);
            
            java.math.BigDecimal planQty = new java.math.BigDecimal(100 + i * 50 + (int)(Math.random() * 500));
            java.math.BigDecimal actualQty = i % 3 == 2 ? planQty : planQty.multiply(new java.math.BigDecimal("0.7"));
            
            LocalDate planStartDate = today.minusDays(30 - i);
            LocalDate planEndDate = planStartDate.plusDays(10);
            LocalDate actualStartDate = i % 3 == 0 ? null : planStartDate.plusDays(1);
            LocalDate actualEndDate = i % 3 == 2 ? planEndDate.minusDays(1) : null;
            
            int status = i % 4;
            
            String remark = "生产工单测试数据" + (i + 1);
            
            try {
                jdbcTemplate.update(insertSql,
                    workOrderNo, workOrderName, productCode, productName, "V1.0",
                    planQty, actualQty, planStartDate, planEndDate,
                    actualStartDate, actualEndDate, status, remark
                );
                inserted++;
            } catch (Exception e) {
                log.warn("插入生产工单测试数据失败: {} - {}", workOrderNo, e.getMessage());
            }
        }
        
        log.info("已插入{}条生产工单测试数据", inserted);
    }

    private void checkAndCreatePurchaseOrderItemTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_purchase_order_item'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_purchase_order_item (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        order_id BIGINT COMMENT '采购订单ID',
                        material_code VARCHAR(50) COMMENT '物料编码',
                        material_name VARCHAR(200) COMMENT '物料名称',
                        material_spec VARCHAR(500) COMMENT '物料规格',
                        material_unit VARCHAR(20) COMMENT '物料单位',
                        material_category VARCHAR(100) COMMENT '物料类别',
                        quantity DECIMAL(18,2) COMMENT '订单数量',
                        received_quantity DECIMAL(18,2) COMMENT '已收货数量',
                        unit_price DECIMAL(18,2) COMMENT '单价',
                        total_price DECIMAL(18,2) COMMENT '总金额',
                        delivery_date DATE COMMENT '交货日期',
                        remark VARCHAR(500) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_order_id (order_id),
                        KEY idx_material_code (material_code)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建采购订单明细表成功: scm_purchase_order_item");
            } else {
                log.debug("采购订单明细表已存在: scm_purchase_order_item");
            }
        } catch (Exception e) {
            log.error("检查或创建采购订单明细表失败", e);
        }
    }

    private void checkAndCreateOrderChangeTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_order_change'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_order_change (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        change_no VARCHAR(50) COMMENT '变更单号',
                        order_id BIGINT COMMENT '采购订单ID',
                        order_no VARCHAR(50) COMMENT '订单编号',
                        change_type TINYINT COMMENT '变更类型：1数量变更 2价格变更 3交货日期变更 4取消订单 5其他',
                        change_reason VARCHAR(500) COMMENT '变更原因',
                        change_content TEXT COMMENT '变更内容(JSON)',
                        old_content TEXT COMMENT '原内容(JSON)',
                        status TINYINT DEFAULT 0 COMMENT '状态：0待提交 1待审批 2审批通过 3审批拒绝 4已取消',
                        apply_time DATETIME COMMENT '申请时间',
                        apply_by VARCHAR(50) COMMENT '申请人',
                        approve_time DATETIME COMMENT '审批时间',
                        approve_by VARCHAR(50) COMMENT '审批人',
                        approve_remark VARCHAR(1000) COMMENT '审批备注',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_change_no (change_no),
                        KEY idx_order_id (order_id),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单变更记录表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建订单变更记录表成功: scm_order_change");
            } else {
                log.debug("订单变更记录表已存在: scm_order_change");
            }
        } catch (Exception e) {
            log.error("检查或创建订单变更记录表失败", e);
        }
    }

    private void checkAndCreateProductionProgressTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_production_progress'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count != null && count > 0) {
                jdbcTemplate.execute("DROP TABLE IF EXISTS scm_production_progress");
                log.info("删除旧的生产进度表: scm_production_progress");
            }
            
            String createSql = """
                CREATE TABLE scm_production_progress (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                    order_id BIGINT COMMENT '采购订单ID',
                    order_no VARCHAR(50) COMMENT '订单编号',
                    order_item_id BIGINT COMMENT '订单明细ID',
                    material_code VARCHAR(50) COMMENT '物料编码',
                    material_name VARCHAR(200) COMMENT '物料名称',
                    material_spec VARCHAR(500) COMMENT '物料规格',
                    material_unit VARCHAR(20) COMMENT '物料单位',
                    total_quantity DECIMAL(18,2) COMMENT '总数量',
                    completed_quantity DECIMAL(18,2) COMMENT '已完成数量',
                    progress_rate DECIMAL(10,2) COMMENT '进度百分比',
                    progress_status TINYINT DEFAULT 0 COMMENT '进度状态：0待开始 1进行中 2已完成 3已暂停 4已延误',
                    work_station VARCHAR(100) COMMENT '生产工位',
                    responsible_person VARCHAR(50) COMMENT '负责人',
                    estimated_start_date DATE COMMENT '预计开始日期',
                    actual_start_date DATE COMMENT '实际开始日期',
                    estimated_finish_date DATE COMMENT '预计完成日期',
                    actual_finish_date DATE COMMENT '实际完成日期',
                    remark VARCHAR(1000) COMMENT '备注',
                    is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                    create_by VARCHAR(50) COMMENT '创建人',
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    update_by VARCHAR(50) COMMENT '更新人',
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_order_id (order_id),
                    KEY idx_progress_status (progress_status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产进度表'
                """;
            jdbcTemplate.execute(createSql);
            log.info("创建生产进度表成功: scm_production_progress");
        } catch (Exception e) {
            log.error("检查或创建生产进度表失败", e);
        }
    }

    private void checkAndCreateShipmentTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_shipment'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count != null && count > 0) {
                jdbcTemplate.execute("DROP TABLE IF EXISTS scm_shipment");
                log.info("删除旧的发货记录表: scm_shipment");
            }
            
            String createSql = """
                CREATE TABLE scm_shipment (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                    shipment_no VARCHAR(50) COMMENT '发货单号',
                    order_id BIGINT COMMENT '采购订单ID',
                    order_no VARCHAR(50) COMMENT '订单编号',
                    supplier_id BIGINT COMMENT '供应商ID',
                    supplier_name VARCHAR(200) COMMENT '供应商名称',
                    shipment_type TINYINT COMMENT '发货类型：1正常发货 2紧急发货 3补发',
                    shipment_date DATE COMMENT '发货日期',
                    estimated_arrival_date DATE COMMENT '预计到达日期',
                    actual_arrival_date DATE COMMENT '实际到达日期',
                    item_count INT COMMENT '物料种类数量',
                    total_quantity DECIMAL(18,2) COMMENT '发货总数量',
                    total_weight DECIMAL(18,2) COMMENT '总重量',
                    shipping_method VARCHAR(100) COMMENT '运输方式',
                    carrier VARCHAR(100) COMMENT '承运人/物流公司',
                    waybill_no VARCHAR(100) COMMENT '运单号',
                    departure_place VARCHAR(200) COMMENT '发货地',
                    destination VARCHAR(500) COMMENT '目的地',
                    contact_person VARCHAR(50) COMMENT '联系人',
                    contact_phone VARCHAR(20) COMMENT '联系电话',
                    status TINYINT DEFAULT 0 COMMENT '状态：0待发货 1已发货 2运输中 3已送达 4已签收',
                    remark VARCHAR(1000) COMMENT '备注',
                    is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                    create_by VARCHAR(50) COMMENT '创建人',
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    update_by VARCHAR(50) COMMENT '更新人',
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_shipment_no (shipment_no),
                    KEY idx_order_id (order_id),
                    KEY idx_status (status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发货记录表'
                """;
            jdbcTemplate.execute(createSql);
            log.info("创建发货记录表成功: scm_shipment");
        } catch (Exception e) {
            log.error("检查或创建发货记录表失败", e);
        }
    }

    private void checkAndCreateLogisticsTrackTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_logistics_track'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_logistics_track (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        shipment_id BIGINT COMMENT '发货单ID',
                        shipment_no VARCHAR(50) COMMENT '发货单号',
                        tracking_no VARCHAR(100) COMMENT '物流单号',
                        track_time DATETIME COMMENT '轨迹时间',
                        location VARCHAR(200) COMMENT '当前位置',
                        description VARCHAR(500) COMMENT '轨迹描述',
                        status TINYINT COMMENT '物流状态：0待发货 1已发货 2运输中 3已送达 4已签收',
                        operator VARCHAR(50) COMMENT '操作人',
                        remark VARCHAR(500) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_shipment_id (shipment_id),
                        KEY idx_tracking_no (tracking_no)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建物流轨迹表成功: scm_logistics_track");
            } else {
                log.debug("物流轨迹表已存在: scm_logistics_track");
            }
        } catch (Exception e) {
            log.error("检查或创建物流轨迹表失败", e);
        }
    }

    private void checkAndCreateDeliveryAppointmentTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_delivery_appointment'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_delivery_appointment (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        appointment_no VARCHAR(50) COMMENT '预约单号',
                        order_id BIGINT COMMENT '采购订单ID',
                        order_no VARCHAR(50) COMMENT '订单编号',
                        shipment_id BIGINT COMMENT '发货单ID',
                        supplier_id BIGINT COMMENT '供应商ID',
                        supplier_name VARCHAR(200) COMMENT '供应商名称',
                        delivery_date DATE COMMENT '预约送货日期',
                        time_slot VARCHAR(50) COMMENT '预约时间段',
                        warehouse_code VARCHAR(50) COMMENT '仓库编码',
                        warehouse_name VARCHAR(100) COMMENT '仓库名称',
                        contact_person VARCHAR(50) COMMENT '联系人',
                        contact_phone VARCHAR(20) COMMENT '联系电话',
                        item_count INT COMMENT '物料种类数量',
                        total_quantity DECIMAL(18,2) COMMENT '总数量',
                        vehicle_no VARCHAR(50) COMMENT '车牌号',
                        driver_name VARCHAR(50) COMMENT '司机姓名',
                        driver_phone VARCHAR(20) COMMENT '司机电话',
                        status TINYINT DEFAULT 0 COMMENT '状态：0待确认 1已确认 2已签到 3已完成 4已取消',
                        check_in_time DATETIME COMMENT '签到时间',
                        check_out_time DATETIME COMMENT '签退时间',
                        warehouse_operator VARCHAR(50) COMMENT '仓库操作员',
                        remark VARCHAR(1000) COMMENT '备注',
                        cancel_reason VARCHAR(500) COMMENT '取消原因',
                        cancel_time DATETIME COMMENT '取消时间',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_appointment_no (appointment_no),
                        KEY idx_order_id (order_id),
                        KEY idx_delivery_date (delivery_date),
                        KEY idx_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='送货预约表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建送货预约表成功: scm_delivery_appointment");
            } else {
                log.debug("送货预约表已存在: scm_delivery_appointment");
            }
        } catch (Exception e) {
            log.error("检查或创建送货预约表失败", e);
        }
    }

    private void checkAndCreateIncomingInspectionTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'scm_incoming_inspection'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                String createSql = """
                    CREATE TABLE scm_incoming_inspection (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        inspection_no VARCHAR(50) COMMENT '质检单号',
                        order_id BIGINT COMMENT '采购订单ID',
                        order_no VARCHAR(50) COMMENT '订单编号',
                        order_item_id BIGINT COMMENT '订单明细ID',
                        shipment_id BIGINT COMMENT '发货单ID',
                        material_code VARCHAR(50) COMMENT '物料编码',
                        material_name VARCHAR(200) COMMENT '物料名称',
                        material_spec VARCHAR(500) COMMENT '物料规格',
                        material_unit VARCHAR(20) COMMENT '物料单位',
                        supplier_id BIGINT COMMENT '供应商ID',
                        supplier_name VARCHAR(200) COMMENT '供应商名称',
                        batch_no VARCHAR(100) COMMENT '批次号',
                        inspection_quantity DECIMAL(18,2) COMMENT '检验数量',
                        sampling_quantity DECIMAL(18,2) COMMENT '抽样数量',
                        qualified_quantity DECIMAL(18,2) COMMENT '合格数量',
                        unqualified_quantity DECIMAL(18,2) COMMENT '不合格数量',
                        pass_rate DECIMAL(10,2) COMMENT '合格率',
                        result TINYINT DEFAULT 0 COMMENT '检验结果：0待检验 1合格 2让步接收 3退货 4待复检',
                        inspection_time DATETIME COMMENT '检验时间',
                        inspector VARCHAR(50) COMMENT '检验员',
                        submit_time DATETIME COMMENT '提交时间',
                        approve_time DATETIME COMMENT '审核时间',
                        approver VARCHAR(50) COMMENT '审核人',
                        handling_type TINYINT COMMENT '处理方式：1入库 2退货 3让步接收 4待复检',
                        inspection_items TEXT COMMENT '检验项明细(JSON)',
                        defect_description TEXT COMMENT '缺陷描述',
                        photo_urls TEXT COMMENT '照片URL列表',
                        remark VARCHAR(1000) COMMENT '备注',
                        is_deleted TINYINT DEFAULT 0 COMMENT '软删除',
                        create_by VARCHAR(50) COMMENT '创建人',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_by VARCHAR(50) COMMENT '更新人',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (id),
                        KEY idx_inspection_no (inspection_no),
                        KEY idx_order_id (order_id),
                        KEY idx_result (result)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='来料质检表'
                    """;
                jdbcTemplate.execute(createSql);
                log.info("创建来料质检表成功: scm_incoming_inspection");
            } else {
                log.debug("来料质检表已存在: scm_incoming_inspection");
            }
        } catch (Exception e) {
            log.error("检查或创建来料质检表失败", e);
        }
    }

    private void insertOrderExecutionTestData() {
        try {
            jdbcTemplate.execute("SET NAMES utf8mb4");
            jdbcTemplate.execute("SET CHARACTER SET utf8mb4");
            
            String orderCountSql = "SELECT COUNT(*) FROM scm_purchase_order WHERE is_deleted = 0";
            Integer orderCount = jdbcTemplate.queryForObject(orderCountSql, Integer.class);
            
            if (orderCount == null || orderCount < 30) {
                insertPurchaseOrderTestData();
            } else {
                log.info("采购订单测试数据已存在: {}条", orderCount);
            }
            
            String orderItemCountSql = "SELECT COUNT(*) FROM scm_purchase_order_item WHERE is_deleted = 0";
            Integer orderItemCount = jdbcTemplate.queryForObject(orderItemCountSql, Integer.class);
            
            if (orderItemCount == null || orderItemCount < 50) {
                insertPurchaseOrderItemTestData();
            } else {
                log.info("采购订单明细测试数据已存在: {}条", orderItemCount);
            }
            
            String changeCountSql = "SELECT COUNT(*) FROM scm_order_change WHERE is_deleted = 0";
            Integer changeCount = jdbcTemplate.queryForObject(changeCountSql, Integer.class);
            
            if (changeCount == null || changeCount < 20) {
                insertOrderChangeTestData();
            } else {
                log.info("订单变更测试数据已存在: {}条", changeCount);
            }
            
            String progressCountSql = "SELECT COUNT(*) FROM scm_production_progress WHERE is_deleted = 0";
            Integer progressCount = jdbcTemplate.queryForObject(progressCountSql, Integer.class);
            
            if (progressCount == null || progressCount < 30) {
                insertProductionProgressTestData();
            } else {
                log.info("生产进度测试数据已存在: {}条", progressCount);
            }
            
            String shipmentCountSql = "SELECT COUNT(*) FROM scm_shipment WHERE is_deleted = 0";
            Integer shipmentCount = jdbcTemplate.queryForObject(shipmentCountSql, Integer.class);
            
            if (shipmentCount == null || shipmentCount < 20) {
                insertShipmentTestData();
            } else {
                log.info("发货记录测试数据已存在: {}条", shipmentCount);
            }
            
            String appointmentCountSql = "SELECT COUNT(*) FROM scm_delivery_appointment WHERE is_deleted = 0";
            Integer appointmentCount = jdbcTemplate.queryForObject(appointmentCountSql, Integer.class);
            
            if (appointmentCount == null || appointmentCount < 20) {
                insertDeliveryAppointmentTestData();
            } else {
                log.info("送货预约测试数据已存在: {}条", appointmentCount);
            }
            
            String inspectionCountSql = "SELECT COUNT(*) FROM scm_incoming_inspection WHERE is_deleted = 0";
            Integer inspectionCount = jdbcTemplate.queryForObject(inspectionCountSql, Integer.class);
            
            if (inspectionCount == null || inspectionCount < 20) {
                insertIncomingInspectionTestData();
            } else {
                log.info("来料质检测试数据已存在: {}条", inspectionCount);
            }
            
        } catch (Exception e) {
            log.error("插入订单执行测试数据失败", e);
        }
    }

    private void insertPurchaseOrderTestData() {
        try {
            List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                "SELECT id, supplier_code, supplier_name FROM scm_supplier WHERE is_deleted = 0 ORDER BY id"
            );
            
            if (suppliers.isEmpty()) {
                log.warn("没有供应商数据，无法生成采购订单测试数据");
                return;
            }
            
            LocalDate today = LocalDate.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
            
            String[] orderNames = {
                "不锈钢钢板采购订单", "铝合金型材采购订单", "电子元器件采购订单",
                "电机马达采购订单", "传感器采购订单", "控制器采购订单",
                "电缆线材采购订单", "密封件采购订单", "轴承采购订单",
                "润滑油采购订单", "化工原料采购订单", "包装材料采购订单",
                "紧固件采购订单", "液压元件采购订单", "气动元件采购订单",
                "电气开关采购订单", "变压器采购订单", "电容器采购订单",
                "电阻器采购订单", "连接器采购订单", "散热器采购订单",
                "风扇采购订单", "水泵采购订单", "阀门采购订单",
                "过滤器采购订单", "压力表采购订单", "流量计采购订单",
                "温度传感器采购订单", "压力传感器采购订单", "生产线设备采购订单"
            };
            
            String[] paymentTerms = {"月结30天", "月结60天", "预付30%货到付70%", "款到发货", "月结45天"};
            String[] deliveryTerms = {"送货上门", "自提", "物流配送", "快递", "上门自提"};
            
            String insertSql = """
                INSERT INTO scm_purchase_order 
                (order_no, order_name, supplier_id, supplier_code, supplier_name,
                 order_type, order_date, expected_delivery_date, item_count,
                 total_quantity, total_amount, payment_terms, delivery_terms,
                 delivery_address, contact_person, contact_phone, status, remark, is_deleted, create_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
                """;
            
            int inserted = 0;
            
            for (int i = 0; i < 30; i++) {
                int supplierIndex = i % suppliers.size();
                Map<String, Object> supplier = suppliers.get(supplierIndex);
                Long supplierId = ((Number) supplier.get("id")).longValue();
                String supplierCode = (String) supplier.get("supplier_code");
                String supplierName = (String) supplier.get("supplier_name");
                
                String orderNo = "PO" + today.minusDays(30 - i).format(formatter) + String.format("%05d", i + 1);
                String orderName = orderNames[i];
                int orderType = (i % 3) + 1;
                LocalDate orderDate = today.minusDays(30 - i);
                LocalDate expectedDate = orderDate.plusDays(15 + i % 10);
                int itemCount = 1 + (i % 3);
                java.math.BigDecimal totalQty = new java.math.BigDecimal(100 + i * 50);
                java.math.BigDecimal totalAmount = new java.math.BigDecimal(10000 + i * 5000);
                String paymentTerm = paymentTerms[i % paymentTerms.length];
                String deliveryTerm = deliveryTerms[i % deliveryTerms.length];
                String address = "某某市某某区某某工业园区A栋仓库" + ((i % 5) + 1) + "号";
                String contactPerson = "仓库管理员" + ((i % 4) + 1);
                String contactPhone = "010-8888" + String.format("%04d", 1000 + i);
                int status = i % 10;
                
                String remark = "采购订单测试数据" + (i + 1) + " - 供应商: " + supplierName;
                
                try {
                    jdbcTemplate.update(insertSql,
                        orderNo, orderName, supplierId, supplierCode, supplierName,
                        orderType, orderDate, expectedDate, itemCount,
                        totalQty, totalAmount, paymentTerm, deliveryTerm,
                        address, contactPerson, contactPhone, status, remark
                    );
                    inserted++;
                } catch (Exception e) {
                    log.warn("插入采购订单测试数据失败: {} - {}", orderNo, e.getMessage());
                }
            }
            
            log.info("已插入{}条采购订单测试数据", inserted);
        } catch (Exception e) {
            log.error("插入采购订单测试数据失败", e);
        }
    }

    private void insertPurchaseOrderItemTestData() {
        try {
            List<Map<String, Object>> orders = jdbcTemplate.queryForList(
                "SELECT id, order_no, order_name FROM scm_purchase_order WHERE is_deleted = 0 ORDER BY id"
            );
            
            if (orders.isEmpty()) {
                log.warn("没有采购订单数据，无法生成采购订单明细测试数据");
                return;
            }
            
            String[] materialNames = {
                "不锈钢板", "铝型材", "铜排", "塑料颗粒", "橡胶密封条",
                "电子元器件", "集成电路芯片", "电阻电容", "连接器", "电缆线",
                "液压油", "润滑油", "冷却液", "清洗剂", "防锈剂",
                "轴承", "齿轮", "密封圈", "紧固件", "弹簧"
            };
            
            String[] specs = {"304-2mm", "6061-T5", "T2-3x20", "PP-500", "EPDM-10x5", "SMT-0805", "DIP-DIP8", "SMD-0603", "RJ45-8P8C", "RVV-3x1.5"};
            String[] units = {"张", "件", "米", "千克", "个", "套", "台", "卷", "箱", "包"};
            
            String insertSql = """
                INSERT INTO scm_purchase_order_item 
                (order_id, material_code, material_name, material_spec, material_unit, material_category,
                 quantity, received_quantity, unit_price, total_price, delivery_date, remark, is_deleted, create_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
                """;
            
            int inserted = 0;
            
            for (int i = 0; i < orders.size(); i++) {
                Map<String, Object> order = orders.get(i);
                Long orderId = ((Number) order.get("id")).longValue();
                
                int itemCount = 1 + (i % 3);
                
                for (int j = 0; j < itemCount; j++) {
                    int materialIndex = (i * 2 + j) % materialNames.length;
                    String materialName = materialNames[materialIndex];
                    String materialCode = "MAT" + String.format("%03d", materialIndex + 1);
                    String spec = specs[materialIndex % specs.length];
                    String unit = units[materialIndex % units.length];
                    String category = String.valueOf((materialIndex % 3) + 1);
                    
                    java.math.BigDecimal quantity = new java.math.BigDecimal(50 + materialIndex * 30);
                    java.math.BigDecimal receivedQty = i % 2 == 0 ? quantity : quantity.multiply(new java.math.BigDecimal("0.5"));
                    java.math.BigDecimal unitPrice = new java.math.BigDecimal(100 + materialIndex * 50);
                    java.math.BigDecimal totalPrice = quantity.multiply(unitPrice);
                    LocalDate deliveryDate = LocalDate.now().minusDays(15 - i);
                    
                    String remark = "采购订单明细测试数据" + (inserted + 1);
                    
                    try {
                        jdbcTemplate.update(insertSql,
                            orderId, materialCode, materialName, spec, unit, category,
                            quantity, receivedQty, unitPrice, totalPrice, deliveryDate, remark
                        );
                        inserted++;
                    } catch (Exception e) {
                        log.warn("插入采购订单明细测试数据失败: {} - {}", orderId, e.getMessage());
                    }
                }
            }
            
            log.info("已插入{}条采购订单明细测试数据", inserted);
        } catch (Exception e) {
            log.error("插入采购订单明细测试数据失败", e);
        }
    }

    private void insertOrderChangeTestData() {
        try {
            List<Map<String, Object>> orders = jdbcTemplate.queryForList(
                "SELECT id, order_no FROM scm_purchase_order WHERE is_deleted = 0 ORDER BY id LIMIT 20"
            );
            
            if (orders.isEmpty()) {
                log.warn("没有采购订单数据，无法生成订单变更测试数据");
                return;
            }
            
            LocalDate today = LocalDate.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
            
            String[] reasons = {
                "客户需求变更", "价格调整", "交货日期提前",
                "交货日期延后", "数量调整", "规格变更",
                "质量问题需要更换", "供应商产能不足", "运输问题调整",
                "库存调整", "项目延期", "预算调整"
            };
            
            String insertSql = """
                INSERT INTO scm_order_change 
                (change_no, order_id, order_no, change_type, change_reason,
                 status, apply_time, apply_by, remark, is_deleted, create_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
                """;
            
            int inserted = 0;
            
            for (int i = 0; i < Math.min(20, orders.size()); i++) {
                Map<String, Object> order = orders.get(i);
                Long orderId = ((Number) order.get("id")).longValue();
                String orderNo = (String) order.get("order_no");
                
                String changeNo = "OC" + today.minusDays(20 - i).format(formatter) + String.format("%04d", i + 1);
                int changeType = (i % 5) + 1;
                String reason = reasons[i % reasons.length];
                int status = i % 5;
                LocalDateTime applyTime = LocalDateTime.now().minusDays(20 - i);
                
                String remark = "订单变更测试数据" + (i + 1);
                
                try {
                    jdbcTemplate.update(insertSql,
                        changeNo, orderId, orderNo, changeType, reason,
                        status, applyTime, "采购专员" + (i % 4 + 1), remark
                    );
                    inserted++;
                } catch (Exception e) {
                    log.warn("插入订单变更测试数据失败: {} - {}", changeNo, e.getMessage());
                }
            }
            
            log.info("已插入{}条订单变更测试数据", inserted);
        } catch (Exception e) {
            log.error("插入订单变更测试数据失败", e);
        }
    }

    private void insertProductionProgressTestData() {
        try {
            List<Map<String, Object>> orderItems = jdbcTemplate.queryForList(
                "SELECT oi.id, oi.order_id, o.order_no, oi.material_name, oi.material_spec, oi.quantity " +
                "FROM scm_purchase_order_item oi " +
                "JOIN scm_purchase_order o ON oi.order_id = o.id " +
                "WHERE oi.is_deleted = 0 ORDER BY oi.id"
            );
            
            if (orderItems.isEmpty()) {
                log.warn("没有采购订单明细数据，无法生成生产进度测试数据");
                return;
            }
            
            String insertSql = """
                INSERT INTO scm_production_progress 
                (order_id, order_no, order_item_id, material_name, material_spec,
                 total_quantity, completed_quantity, progress_rate, status,
                 planned_start_date, planned_end_date, remark, is_deleted, create_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
                """;
            
            int inserted = 0;
            
            for (int i = 0; i < Math.min(30, orderItems.size()); i++) {
                Map<String, Object> item = orderItems.get(i);
                Long orderItemId = ((Number) item.get("id")).longValue();
                Long orderId = ((Number) item.get("order_id")).longValue();
                String orderNo = (String) item.get("order_no");
                String materialName = (String) item.get("material_name");
                String materialSpec = (String) item.get("material_spec");
                java.math.BigDecimal totalQty = (java.math.BigDecimal) item.get("quantity");
                
                java.math.BigDecimal completedRate = new java.math.BigDecimal(0.1 + i * 0.03);
                if (completedRate.compareTo(java.math.BigDecimal.ONE) > 0) {
                    completedRate = java.math.BigDecimal.ONE;
                }
                java.math.BigDecimal completedQty = totalQty.multiply(completedRate);
                java.math.BigDecimal progressRate = completedRate.multiply(new java.math.BigDecimal(100)).setScale(2, java.math.RoundingMode.HALF_UP);
                
                int status;
                if (progressRate.compareTo(java.math.BigDecimal.ZERO) == 0) {
                    status = 0;
                } else if (progressRate.compareTo(java.math.BigDecimal.valueOf(100)) == 0) {
                    status = 2;
                } else {
                    status = 1;
                }
                
                LocalDate plannedStart = LocalDate.now().minusDays(30 - i);
                LocalDate plannedEnd = plannedStart.plusDays(20);
                
                String remark = "生产进度测试数据" + (i + 1);
                
                try {
                    jdbcTemplate.update(insertSql,
                        orderId, orderNo, orderItemId, materialName, materialSpec,
                        totalQty, completedQty, progressRate, status,
                        plannedStart, plannedEnd, remark
                    );
                    inserted++;
                } catch (Exception e) {
                    log.warn("插入生产进度测试数据失败: {} - {}", orderNo, e.getMessage());
                }
            }
            
            log.info("已插入{}条生产进度测试数据", inserted);
        } catch (Exception e) {
            log.error("插入生产进度测试数据失败", e);
        }
    }

    private void insertShipmentTestData() {
        try {
            List<Map<String, Object>> orders = jdbcTemplate.queryForList(
                "SELECT o.id, o.order_no, o.supplier_id, o.supplier_name, o.expected_delivery_date " +
                "FROM scm_purchase_order o WHERE o.is_deleted = 0 AND o.status >= 4 ORDER BY o.id LIMIT 20"
            );
            
            if (orders.isEmpty()) {
                log.warn("没有已确认的采购订单数据，无法生发货记录测试数据");
                return;
            }
            
            LocalDate today = LocalDate.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
            
            String[] logisticsCompanies = {"顺丰速运", "德邦物流", "京东物流", "中通快递", "圆通速递", "申通快递", "韵达快递", "极兔速递"};
            
            String insertSql = """
                INSERT INTO scm_shipment 
                (shipment_no, order_id, order_no, supplier_id, supplier_name,
                 total_quantity, shipment_date, estimated_arrival_date,
                 logistics_company, tracking_no, status, remark, is_deleted, create_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
                """;
            
            int inserted = 0;
            
            for (int i = 0; i < Math.min(20, orders.size()); i++) {
                Map<String, Object> order = orders.get(i);
                Long orderId = ((Number) order.get("id")).longValue();
                String orderNo = (String) order.get("order_no");
                Long supplierId = ((Number) order.get("supplier_id")).longValue();
                String supplierName = (String) order.get("supplier_name");
                
                String shipmentNo = "SH" + today.minusDays(20 - i).format(formatter) + String.format("%04d", i + 1);
                java.math.BigDecimal totalQty = new java.math.BigDecimal(50 + i * 30);
                LocalDate shipmentDate = today.minusDays(20 - i);
                LocalDate estimatedArrival = shipmentDate.plusDays(3 + i % 5);
                String logisticsCompany = logisticsCompanies[i % logisticsCompanies.length];
                String trackingNo = "SF" + today.minusDays(20 - i).format(formatter) + String.format("%08d", 10000000 + i);
                int status = i % 5;
                
                String remark = "发货记录测试数据" + (i + 1);
                
                try {
                    jdbcTemplate.update(insertSql,
                        shipmentNo, orderId, orderNo, supplierId, supplierName,
                        totalQty, shipmentDate, estimatedArrival,
                        logisticsCompany, trackingNo, status, remark
                    );
                    inserted++;
                } catch (Exception e) {
                    log.warn("插入发货记录测试数据失败: {} - {}", shipmentNo, e.getMessage());
                }
            }
            
            log.info("已插入{}条发货记录测试数据", inserted);
        } catch (Exception e) {
            log.error("插入发货记录测试数据失败", e);
        }
    }

    private void insertDeliveryAppointmentTestData() {
        try {
            List<Map<String, Object>> orders = jdbcTemplate.queryForList(
                "SELECT o.id, o.order_no, o.supplier_id, o.supplier_name, o.delivery_address " +
                "FROM scm_purchase_order o WHERE o.is_deleted = 0 ORDER BY o.id LIMIT 20"
            );
            
            if (orders.isEmpty()) {
                log.warn("没有采购订单数据，无法生成送货预约测试数据");
                return;
            }
            
            LocalDate today = LocalDate.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
            
            String[] timeSlots = {"09:00-10:00", "10:00-11:00", "11:00-12:00", "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00"};
            String[] warehouses = {"A仓库-原材料区", "B仓库-半成品区", "C仓库-成品区", "D仓库-备件区"};
            String[] contactPersons = {"张师傅", "李师傅", "王师傅", "赵师傅"};
            
            String insertSql = """
                INSERT INTO scm_delivery_appointment 
                (appointment_no, order_id, order_no, supplier_id, supplier_name,
                 delivery_date, time_slot, warehouse_name, contact_person, contact_phone,
                 item_count, total_quantity, vehicle_no, driver_name, driver_phone,
                 status, remark, is_deleted, create_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
                """;
            
            int inserted = 0;
            
            for (int i = 0; i < Math.min(20, orders.size()); i++) {
                Map<String, Object> order = orders.get(i);
                Long orderId = ((Number) order.get("id")).longValue();
                String orderNo = (String) order.get("order_no");
                Long supplierId = ((Number) order.get("supplier_id")).longValue();
                String supplierName = (String) order.get("supplier_name");
                
                String appointmentNo = "DA" + today.minusDays(15 - i).format(formatter) + String.format("%04d", i + 1);
                LocalDate deliveryDate = today.plusDays(i % 10);
                String timeSlot = timeSlots[i % timeSlots.length];
                String warehouse = warehouses[i % warehouses.length];
                String contactPerson = contactPersons[i % contactPersons.length];
                String contactPhone = "13800" + String.format("%06d", 138000 + i);
                int itemCount = 1 + (i % 3);
                java.math.BigDecimal totalQty = new java.math.BigDecimal(100 + i * 50);
                String vehicleNo = "京A" + String.format("%05d", 10000 + i);
                String driverName = "司机" + (i % 4 + 1);
                String driverPhone = "13900" + String.format("%06d", 139000 + i);
                int status = i % 5;
                
                String remark = "送货预约测试数据" + (i + 1);
                
                try {
                    jdbcTemplate.update(insertSql,
                        appointmentNo, orderId, orderNo, supplierId, supplierName,
                        deliveryDate, timeSlot, warehouse, contactPerson, contactPhone,
                        itemCount, totalQty, vehicleNo, driverName, driverPhone,
                        status, remark
                    );
                    inserted++;
                } catch (Exception e) {
                    log.warn("插入送货预约测试数据失败: {} - {}", appointmentNo, e.getMessage());
                }
            }
            
            log.info("已插入{}条送货预约测试数据", inserted);
        } catch (Exception e) {
            log.error("插入送货预约测试数据失败", e);
        }
    }

    private void insertIncomingInspectionTestData() {
        try {
            List<Map<String, Object>> orderItems = jdbcTemplate.queryForList(
                "SELECT oi.id, oi.order_id, o.order_no, oi.material_code, oi.material_name, oi.material_spec, oi.material_unit, " +
                "o.supplier_id, o.supplier_name, oi.quantity " +
                "FROM scm_purchase_order_item oi " +
                "JOIN scm_purchase_order o ON oi.order_id = o.id " +
                "WHERE oi.is_deleted = 0 ORDER BY oi.id LIMIT 20"
            );
            
            if (orderItems.isEmpty()) {
                log.warn("没有采购订单明细数据，无法生成来料质检测试数据");
                return;
            }
            
            LocalDate today = LocalDate.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
            
            String[] inspectors = {"质检员张三", "质检员李四", "质检员王五", "质检员赵六"};
            String[] batchNos = {"BATCH202401", "BATCH202402", "BATCH202403", "BATCH202404", "BATCH202405"};
            
            String insertSql = """
                INSERT INTO scm_incoming_inspection 
                (inspection_no, order_id, order_no, order_item_id, material_code, material_name,
                 material_spec, material_unit, supplier_id, supplier_name, batch_no,
                 inspection_quantity, sampling_quantity, qualified_quantity, unqualified_quantity,
                 pass_rate, result, inspector, remark, is_deleted, create_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')
                """;
            
            int inserted = 0;
            
            for (int i = 0; i < Math.min(20, orderItems.size()); i++) {
                Map<String, Object> item = orderItems.get(i);
                Long orderItemId = ((Number) item.get("id")).longValue();
                Long orderId = ((Number) item.get("order_id")).longValue();
                String orderNo = (String) item.get("order_no");
                String materialCode = (String) item.get("material_code");
                String materialName = (String) item.get("material_name");
                String materialSpec = (String) item.get("material_spec");
                String materialUnit = (String) item.get("material_unit");
                Long supplierId = ((Number) item.get("supplier_id")).longValue();
                String supplierName = (String) item.get("supplier_name");
                java.math.BigDecimal quantity = (java.math.BigDecimal) item.get("quantity");
                
                String inspectionNo = "II" + today.minusDays(15 - i).format(formatter) + String.format("%04d", i + 1);
                String batchNo = batchNos[i % batchNos.length] + String.format("%03d", i);
                
                java.math.BigDecimal inspectionQty = quantity;
                java.math.BigDecimal samplingQty = quantity.multiply(new java.math.BigDecimal("0.1")).setScale(2, java.math.RoundingMode.HALF_UP);
                if (samplingQty.compareTo(java.math.BigDecimal.ONE) < 0) {
                    samplingQty = java.math.BigDecimal.ONE;
                }
                
                java.math.BigDecimal passRate;
                int result;
                
                if (i % 4 == 0) {
                    passRate = new java.math.BigDecimal("100.00");
                    result = 1;
                } else if (i % 4 == 1) {
                    passRate = new java.math.BigDecimal("95.50");
                    result = 2;
                } else if (i % 4 == 2) {
                    passRate = new java.math.BigDecimal("80.00");
                    result = 3;
                } else {
                    passRate = new java.math.BigDecimal("85.00");
                    result = 4;
                }
                
                java.math.BigDecimal qualifiedQty = inspectionQty.multiply(passRate).divide(new java.math.BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal unqualifiedQty = inspectionQty.subtract(qualifiedQty);
                
                String inspector = inspectors[i % inspectors.length];
                String remark = "来料质检测试数据" + (i + 1);
                
                try {
                    jdbcTemplate.update(insertSql,
                        inspectionNo, orderId, orderNo, orderItemId, materialCode, materialName,
                        materialSpec, materialUnit, supplierId, supplierName, batchNo,
                        inspectionQty, samplingQty, qualifiedQty, unqualifiedQty,
                        passRate, result, inspector, remark
                    );
                    inserted++;
                } catch (Exception e) {
                    log.warn("插入来料质检测试数据失败: {} - {}", inspectionNo, e.getMessage());
                }
            }
            
            log.info("已插入{}条来料质检测试数据", inserted);
        } catch (Exception e) {
            log.error("插入来料质检测试数据失败", e);
        }
    }
}