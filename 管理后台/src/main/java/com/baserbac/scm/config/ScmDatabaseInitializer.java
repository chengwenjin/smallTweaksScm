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
}