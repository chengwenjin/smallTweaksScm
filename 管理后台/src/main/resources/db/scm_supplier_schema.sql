-- 供应商全生命周期管理 - 准入与分级管理 - 资质审核相关表
-- 数据库: small_tweaks_scm

-- 1. 供应商基础信息表
CREATE TABLE IF NOT EXISTS `scm_supplier` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `supplier_code` VARCHAR(50) NOT NULL COMMENT '供应商编码',
    `supplier_name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `supplier_type` TINYINT NOT NULL DEFAULT 1 COMMENT '供应商类型：1生产型 2贸易型 3服务型',
    `grade` TINYINT DEFAULT 1 COMMENT '供应商等级：1A级 2AA级 3AAA级',
    `contact_person` VARCHAR(50) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `contact_email` VARCHAR(100) COMMENT '联系邮箱',
    `address` VARCHAR(255) COMMENT '详细地址',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待准入 1已准入 2已冻结 3已淘汰',
    `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝',
    `audit_remark` VARCHAR(500) COMMENT '审核备注',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `ext1` VARCHAR(255) COMMENT '预留扩展字段1',
    `ext2` VARCHAR(255) COMMENT '预留扩展字段2',
    `ext3` VARCHAR(255) COMMENT '预留扩展字段3',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_supplier_code` (`supplier_code`),
    KEY `idx_supplier_name` (`supplier_name`),
    KEY `idx_status` (`status`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商基础信息表';

-- 2. 供应商资质审核表
CREATE TABLE IF NOT EXISTS `scm_supplier_qualification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `qualification_type` VARCHAR(50) NOT NULL COMMENT '资质类型：BUSINESS_LICENSE营业执照, TAX_REGISTRATION税务登记证, ORG_CODE组织机构代码证, PRODUCT_CERT产品认证, QUALITY_CERT质量认证, OTHER其他',
    `qualification_name` VARCHAR(100) NOT NULL COMMENT '资质名称',
    `certificate_no` VARCHAR(100) COMMENT '证书编号',
    `issue_date` DATE COMMENT '发证日期',
    `expiry_date` DATE COMMENT '有效期至',
    `is_long_term` TINYINT NOT NULL DEFAULT 0 COMMENT '是否长期有效：0否 1是',
    `file_urls` TEXT COMMENT '附件URL列表，多个用逗号分隔',
    `issuing_authority` VARCHAR(100) COMMENT '发证机关',
    `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝',
    `audit_remark` VARCHAR(500) COMMENT '审核备注',
    `audit_by` VARCHAR(50) COMMENT '审核人',
    `audit_time` DATETIME COMMENT '审核时间',
    `alert_status` TINYINT NOT NULL DEFAULT 0 COMMENT '预警状态：0正常 1即将到期 2已过期',
    `alert_sent` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已发送预警：0否 1是',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_qualification_type` (`qualification_type`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_alert_status` (`alert_status`),
    KEY `idx_expiry_date` (`expiry_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商资质审核表';

-- 3. 资质预警记录表
CREATE TABLE IF NOT EXISTS `scm_qualification_alert` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `qualification_id` BIGINT NOT NULL COMMENT '资质ID',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `alert_type` TINYINT NOT NULL COMMENT '预警类型：1即将到期 2已过期',
    `alert_title` VARCHAR(100) NOT NULL COMMENT '预警标题',
    `alert_content` TEXT COMMENT '预警内容',
    `alert_date` DATE NOT NULL COMMENT '预警日期',
    `days_before_expiry` INT COMMENT '到期前天数（即将到期预警时有效）',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    `read_time` DATETIME COMMENT '阅读时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_qualification_id` (`qualification_id`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资质预警记录表';
