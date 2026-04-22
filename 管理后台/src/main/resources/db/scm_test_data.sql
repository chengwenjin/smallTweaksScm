-- SCM模块测试数据
-- 供应商管理测试数据
INSERT INTO scm_supplier (supplier_code, supplier_name, supplier_type, grade, contact_person, contact_phone, contact_email, address, status, material_category, cooperation_level, create_by, create_time, update_time) VALUES
('SUP001', '华东原材料供应商', 1, 1, '张三', '13800138001', 'zhangsan@example.com', '上海市浦东新区', 1, 1, 1, 'admin', NOW(), NOW()),
('SUP002', '华南辅料供应商', 2, 2, '李四', '13800138002', 'lisi@example.com', '广州市天河区', 1, 2, 2, 'admin', NOW(), NOW()),
('SUP003', '华北设备供应商', 1, 3, '王五', '13800138003', 'wangwu@example.com', '北京市朝阳区', 1, 3, 3, 'admin', NOW(), NOW()),
('SUP004', '华中原材料供应商', 1, 1, '赵六', '13800138004', 'zhaoliu@example.com', '武汉市洪山区', 1, 1, 1, 'admin', NOW(), NOW()),
('SUP005', '西南辅料供应商', 2, 2, '钱七', '13800138005', 'qianqi@example.com', '成都市高新区', 1, 2, 2, 'admin', NOW(), NOW()),
('SUP006', '东北设备供应商', 1, 3, '孙八', '13800138006', 'sunba@example.com', '沈阳市铁西区', 1, 3, 3, 'admin', NOW(), NOW()),
('SUP007', '西北原材料供应商', 1, 2, '周九', '13800138007', 'zhoujiu@example.com', '西安市雁塔区', 1, 1, 2, 'admin', NOW(), NOW()),
('SUP008', '港澳台辅料供应商', 2, 1, '吴十', '13800138008', 'wushi@example.com', '深圳市南山区', 1, 2, 1, 'admin', NOW(), NOW());

-- 资质审核测试数据
INSERT INTO scm_supplier_qualification (supplier_id, qualification_type, qualification_name, certificate_no, issuing_authority, issue_date, expiry_date, is_long_term, file_urls, audit_status, audit_by, audit_time, alert_status, remark, create_by, create_time, update_time) VALUES
(1, 'BUSINESS_LICENSE', '营业执照', '91310000X', '上海市工商行政管理局', '2024-01-15', '2026-01-15', 0, NULL, 2, 'admin', NOW(), 0, '正常在有效期', 'admin', NOW(), NOW()),
(1, 'TAX_REGISTRATION', '税务登记证', '31000000X', '上海市税务局', '2024-01-20', '2026-01-20', 0, NULL, 2, 'admin', NOW(), 0, '正常在有效期', 'admin', NOW(), NOW()),
(2, 'BUSINESS_LICENSE', '营业执照', '91440000X', '广州市工商行政管理局', '2024-02-01', '2025-02-01', 0, NULL, 2, 'admin', NOW(), 1, '即将到期', 'admin', NOW(), NOW()),
(2, 'QUALITY_CERT', 'ISO9001质量认证', 'CNAS001', '中国质量认证中心', '2024-03-01', '2027-03-01', 0, NULL, 2, 'admin', NOW(), 0, '有效期至2027年', 'admin', NOW(), NOW()),
(3, 'BUSINESS_LICENSE', '营业执照', '91110000X', '北京市工商行政管理局', '2023-06-01', '2025-06-01', 0, NULL, 2, 'admin', NOW(), 1, '即将到期', 'admin', NOW(), NOW()),
(3, 'PRODUCT_CERT', '产品3C认证', 'CCC001', '中国质量认证中心', '2024-01-01', NULL, 1, NULL, 2, 'admin', NOW(), 0, '长期有效', 'admin', NOW(), NOW()),
(4, 'BUSINESS_LICENSE', '营业执照', '91420000X', '武汉市工商行政管理局', '2024-04-01', '2026-04-01', 0, NULL, 2, 'admin', NOW(), 0, '正常在有效期', 'admin', NOW(), NOW()),
(5, 'BUSINESS_LICENSE', '营业执照', '91510000X', '成都市工商行政管理局', '2024-05-01', '2025-05-01', 0, NULL, 1, NULL, NULL, 0, '审核中', 'admin', NOW(), NOW()),
(6, 'BUSINESS_LICENSE', '营业执照', '91210000X', '沈阳市工商行政管理局', '2023-12-01', '2024-12-01', 0, NULL, 2, 'admin', NOW(), 2, '已过期', 'admin', NOW(), NOW()),
(7, 'BUSINESS_LICENSE', '营业执照', '91610000X', '西安市工商行政管理局', '2024-06-01', '2026-06-01', 0, NULL, 2, 'admin', NOW(), 0, '正常在有效期', 'admin', NOW(), NOW()),
(8, 'BUSINESS_LICENSE', '营业执照', '91440000Y', '深圳市工商行政管理局', '2024-07-01', '2027-07-01', 0, NULL, 2, 'admin', NOW(), 0, '正常在有效期', 'admin', NOW(), NOW()),
(8, 'TAX_REGISTRATION', '税务登记证', '44030000Y', '深圳市税务局', '2024-07-15', NULL, 1, NULL, 2, 'admin', NOW(), 0, '长期有效', 'admin', NOW(), NOW());

-- 资质预警测试数据
INSERT INTO scm_qualification_alert (supplier_id, qualification_id, alert_type, alert_title, alert_content, days_before_expiry, is_read, alert_date, create_time) VALUES
(2, 3, 1, '资质即将到期提醒', '华南辅料供应商的营业执照将于30天后到期，请及时处理。', 30, 0, DATE_FORMAT(NOW(), '%Y-%m-%d'), NOW()),
(3, 5, 1, '资质即将到期提醒', '华北设备供应商的营业执照将于45天后到期，请及时处理。', 45, 0, DATE_FORMAT(NOW(), '%Y-%m-%d'), NOW()),
(6, 9, 2, '资质已过期提醒', '东北设备供应商的营业执照已过期，请立即处理。', -15, 0, DATE_FORMAT(NOW(), '%Y-%m-%d'), NOW()),
(1, 1, 0, '资质有效期正常', '华东原材料供应商的营业执照有效期正常。', 365, 1, DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY), '%Y-%m-%d'), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 7, 0, '资质有效期正常', '华中原材料供应商的营业执照有效期正常。', 400, 1, DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY), '%Y-%m-%d'), DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 分级分类变更记录测试数据
INSERT INTO scm_supplier_classification_log (supplier_id, old_material_category, new_material_category, old_cooperation_level, new_cooperation_level, change_reason, create_by, create_time) VALUES
(1, NULL, 1, NULL, 1, '初始分级', 'admin', NOW()),
(2, NULL, 2, NULL, 2, '初始分级', 'admin', NOW()),
(3, NULL, 3, NULL, 3, '初始分级', 'admin', NOW()),
(4, NULL, 1, NULL, 1, '初始分级', 'admin', NOW()),
(5, NULL, 2, NULL, 2, '初始分级', 'admin', NOW()),
(6, NULL, 3, NULL, 3, '初始分级', 'admin', NOW()),
(7, NULL, 1, NULL, 2, '初始分级', 'admin', NOW()),
(8, NULL, 2, NULL, 1, '初始分级', 'admin', NOW()),
(1, 1, 1, 1, 2, '合作加深，升级为战略供应商', 'admin', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, 2, 1, 2, 1, '业务拓展，材料类别调整', 'admin', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 3, 2, 3, 2, '合作关系调整', 'admin', DATE_SUB(NOW(), INTERVAL 3 DAY));

COMMIT;