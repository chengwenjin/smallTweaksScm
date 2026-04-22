-- SCM模块完整数据库初始化
-- 数据库: small_tweaks_scm

-- 1. 供应商表
CREATE TABLE IF NOT EXISTS `scm_supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_code` varchar(50) NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(200) NOT NULL COMMENT '供应商名称',
  `supplier_type` tinyint DEFAULT NULL COMMENT '供应商类型：1生产型 2贸易型 3服务型',
  `grade` tinyint DEFAULT NULL COMMENT '等级：1A级 2AA级 3AAA级',
  `material_category` tinyint DEFAULT NULL COMMENT '物资类别：1原材料 2辅料 3设备',
  `cooperation_level` tinyint DEFAULT NULL COMMENT '合作分级：1战略 2合格 3潜在',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `address` varchar(500) DEFAULT NULL COMMENT '地址',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待准入 1已准入 2已冻结 3已淘汰',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '软删除：0未删除 1已删除',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`),
  KEY `idx_supplier_name` (`supplier_name`),
  KEY `idx_status` (`status`),
  KEY `idx_material_category` (`material_category`),
  KEY `idx_cooperation_level` (`cooperation_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商信息表';

-- 2. 供应商资质表
CREATE TABLE IF NOT EXISTS `scm_supplier_qualification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `qualification_type` varchar(50) NOT NULL COMMENT '资质类型：BUSINESS_LICENSE营业执照 TAX_REGISTRATION税务登记证 ORG_CODE组织机构代码证 PRODUCT_CERT产品认证 QUALITY_CERT质量认证 OTHER其他',
  `qualification_name` varchar(200) NOT NULL COMMENT '资质名称',
  `certificate_no` varchar(100) DEFAULT NULL COMMENT '证书编号',
  `issuing_authority` varchar(200) DEFAULT NULL COMMENT '发证机关',
  `issue_date` date DEFAULT NULL COMMENT '发证日期',
  `expiry_date` date DEFAULT NULL COMMENT '有效期至',
  `is_long_term` tinyint NOT NULL DEFAULT '0' COMMENT '长期有效：0否 1是',
  `file_urls` text COMMENT '附件URL，多个用逗号分隔',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝',
  `audit_by` varchar(50) DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(1000) DEFAULT NULL COMMENT '审核备注',
  `alert_status` tinyint NOT NULL DEFAULT '0' COMMENT '预警状态：0正常 1即将到期 2已过期',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '软删除：0未删除 1已删除',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_qualification_type` (`qualification_type`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_alert_status` (`alert_status`),
  KEY `idx_expiry_date` (`expiry_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商资质表';

-- 3. 资质预警表
CREATE TABLE IF NOT EXISTS `scm_qualification_alert` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `qualification_id` bigint NOT NULL COMMENT '资质ID',
  `alert_type` tinyint NOT NULL COMMENT '预警类型：1即将到期 2已过期',
  `alert_title` varchar(200) NOT NULL COMMENT '预警标题',
  `alert_content` varchar(1000) NOT NULL COMMENT '预警内容',
  `days_before_expiry` int DEFAULT NULL COMMENT '到期前天数，负数表示已过期',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：0未读 1已读',
  `alert_date` date NOT NULL COMMENT '预警日期',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '软删除：0未删除 1已删除',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_qualification_id` (`qualification_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_alert_date` (`alert_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资质预警表';

-- 4. 分级分类变更记录表
CREATE TABLE IF NOT EXISTS `scm_supplier_classification_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `old_material_category` tinyint COMMENT '原物资类别',
  `new_material_category` tinyint COMMENT '新物资类别',
  `old_cooperation_level` tinyint COMMENT '原合作分级',
  `new_cooperation_level` tinyint COMMENT '新合作分级',
  `change_reason` varchar(500) COMMENT '变更原因',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '软删除：0未删除 1已删除',
  `create_by` varchar(50) COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商分级分类变更记录表';

-- 5. 为现有表添加必要的索引
ALTER TABLE `scm_supplier` ADD INDEX IF NOT EXISTS `idx_material_category` (`material_category`);
ALTER TABLE `scm_supplier` ADD INDEX IF NOT EXISTS `idx_cooperation_level` (`cooperation_level`);

-- 6. 添加外键约束（可选）
ALTER TABLE `scm_supplier_qualification` ADD CONSTRAINT `fk_qualification_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `scm_supplier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `scm_qualification_alert` ADD CONSTRAINT `fk_alert_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `scm_supplier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `scm_qualification_alert` ADD CONSTRAINT `fk_alert_qualification` FOREIGN KEY (`qualification_id`) REFERENCES `scm_supplier_qualification` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `scm_supplier_classification_log` ADD CONSTRAINT `fk_classification_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `scm_supplier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

COMMIT;