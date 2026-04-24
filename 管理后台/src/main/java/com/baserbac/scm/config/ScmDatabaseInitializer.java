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
            
            log.info("SCM数据库表结构检查完成");
        } catch (Exception e) {
            log.warn("检查表结构失败: {}", e.getMessage());
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