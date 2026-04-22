-- 供应商全生命周期管理 - 准入与分级管理 - 分级分类相关表
-- 数据库: small_tweaks_scm

-- 1. 修改供应商表，添加物资类别和合作分级字段
ALTER TABLE `scm_supplier` 
ADD COLUMN IF NOT EXISTS `material_category` TINYINT DEFAULT NULL COMMENT '物资类别：1原材料 2辅料 3设备' AFTER `grade`,
ADD COLUMN IF NOT EXISTS `cooperation_level` TINYINT DEFAULT NULL COMMENT '合作分级：1战略 2合格 3潜在' AFTER `material_category`;

-- 2. 创建供应商分级分类记录表（用于记录分级变更历史）
CREATE TABLE IF NOT EXISTS `scm_supplier_classification_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `old_material_category` TINYINT COMMENT '原物资类别',
    `new_material_category` TINYINT COMMENT '新物资类别',
    `old_cooperation_level` TINYINT COMMENT '原合作分级',
    `new_cooperation_level` TINYINT COMMENT '新合作分级',
    `change_reason` VARCHAR(500) COMMENT '变更原因',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商分级分类变更记录表';

-- 3. 为新增字段添加索引
ALTER TABLE `scm_supplier` ADD INDEX IF NOT EXISTS `idx_material_category` (`material_category`);
ALTER TABLE `scm_supplier` ADD INDEX IF NOT EXISTS `idx_cooperation_level` (`cooperation_level`);
