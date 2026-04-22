-- SCM模块测试数据生成脚本
-- 执行前请确保数据库表结构已创建

-- 1. 供应商测试数据
INSERT INTO scm_supplier (supplier_code, supplier_name, supplier_type, grade, material_category, cooperation_level, contact_person, contact_phone, contact_email, address, status, remark, create_by, create_time, update_time) VALUES
('SUP001', '华东原材料供应商', 1, 1, 1, 1, '张三', '13800138001', 'zhangsan@example.com', '上海市浦东新区', 1, '优质原材料供应商', 'admin', NOW(), NOW()),
('SUP002', '华南辅料供应商', 2, 2, 2, 2, '李四', '13800138002', 'lisi@example.com', '广州市天河区', 1, '专业辅料供应商', 'admin', NOW(), NOW()),
('SUP003', '华北设备供应商', 1, 3, 3, 3, '王五', '13800138003', 'wangwu@example.com', '北京市朝阳区', 1, '高端设备供应商', 'admin', NOW(), NOW());

-- 2. 资质测试数据
INSERT INTO scm_supplier_qualification (supplier_id, qualification_type, qualification_name, certificate_no, issuing_authority, issue_date, expiry_date, is_long_term, audit_status, alert_status, remark, create_by, create_time, update_time) VALUES
-- 华东原材料供应商的资质
(1, 'BUSINESS_LICENSE', '营业执照', '91310000X001', '上海市工商行政管理局', '2025-01-15', '2027-01-15', 0, 2, 0, '正常在有效期', 'admin', NOW(), NOW()),
(1, 'TAX_REGISTRATION', '税务登记证', '31000000X001', '上海市税务局', '2025-01-20', '2027-01-20', 0, 2, 0, '正常在有效期', 'admin', NOW(), NOW()),
-- 华南辅料供应商的资质
(2, 'BUSINESS_LICENSE', '营业执照', '91440000X002', '广州市工商行政管理局', '2025-02-01', '2026-02-01', 0, 2, 1, '即将到期', 'admin', NOW(), NOW()),
(2, 'QUALITY_CERT', 'ISO9001质量认证', 'CNAS001', '中国质量认证中心', '2025-03-01', '2028-03-01', 0, 2, 0, '有效期至2028年', 'admin', NOW(), NOW()),
-- 华北设备供应商的资质
(3, 'BUSINESS_LICENSE', '营业执照', '91110000X003', '北京市工商行政管理局', '2024-06-01', '2026-06-01', 0, 2, 1, '即将到期', 'admin', NOW(), NOW()),
(3, 'PRODUCT_CERT', '产品3C认证', 'CCC001', '中国质量认证中心', '2025-01-01', NULL, 1, 2, 0, '长期有效', 'admin', NOW(), NOW());

-- 3. 预警测试数据
INSERT INTO scm_qualification_alert (supplier_id, qualification_id, alert_type, alert_title, alert_content, days_before_expiry, is_read, alert_date, create_by, create_time) VALUES
-- 华南辅料供应商营业执照即将到期预警
(2, 3, 1, '资质即将到期提醒', '华南辅料供应商的营业执照将于30天后到期，请及时处理。', 30, 0, DATE_FORMAT(NOW(), '%Y-%m-%d'), 'admin', NOW()),
-- 华北设备供应商营业执照即将到期预警
(3, 5, 1, '资质即将到期提醒', '华北设备供应商的营业执照将于45天后到期，请及时处理。', 45, 0, DATE_FORMAT(NOW(), '%Y-%m-%d'), 'admin', NOW()),
-- 已过期预警（假设存在过期资质）
(1, 1, 2, '资质已过期提醒', '华东原材料供应商的某资质已过期，请立即处理。', -15, 0, DATE_FORMAT(NOW(), '%Y-%m-%d'), 'admin', NOW());

-- 4. 分级分类变更记录测试数据
INSERT INTO scm_supplier_classification_log (supplier_id, old_material_category, new_material_category, old_cooperation_level, new_cooperation_level, change_reason, create_by, create_time) VALUES
-- 初始分级记录
(1, NULL, 1, NULL, 1, '初始分级', 'admin', NOW()),
(2, NULL, 2, NULL, 2, '初始分级', 'admin', NOW()),
(3, NULL, 3, NULL, 3, '初始分级', 'admin', NOW()),
-- 变更记录
(1, 1, 1, 1, 2, '合作加深，升级为战略供应商', 'admin', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, 2, 1, 2, 1, '业务拓展，材料类别调整', 'admin', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 3, 2, 3, 2, '合作关系调整', 'admin', DATE_SUB(NOW(), INTERVAL 3 DAY));

COMMIT;

-- 5. 验证数据插入结果
SELECT '供应商数据' AS type, COUNT(*) AS count FROM scm_supplier;
SELECT '资质数据' AS type, COUNT(*) AS count FROM scm_supplier_qualification;
SELECT '预警数据' AS type, COUNT(*) AS count FROM scm_qualification_alert;
SELECT '分级分类记录' AS type, COUNT(*) AS count FROM scm_supplier_classification_log;