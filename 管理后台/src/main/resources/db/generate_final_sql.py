#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复SCM模块表及字段中文注释乱码问题
使用Python内置的sqlite3方式（备选），或直接输出SQL让用户执行
"""

import sys
import os

print("=" * 60)
print("  SCM数据库中文注释修复工具")
print("=" * 60)

FIX_SQLS = [
"ALTER TABLE scm_supplier COMMENT = '供应商基础信息表';",
"ALTER TABLE scm_supplier_qualification COMMENT = '供应商资质审核表';",
"ALTER TABLE scm_qualification_alert COMMENT = '资质预警记录表';",
"ALTER TABLE scm_supplier_classification_log COMMENT = '供应商分级分类变更记录表';",

"ALTER TABLE scm_supplier MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键';",
"ALTER TABLE scm_supplier MODIFY COLUMN `supplier_code` VARCHAR(50) NOT NULL COMMENT '供应商编码';",
"ALTER TABLE scm_supplier MODIFY COLUMN `supplier_name` VARCHAR(200) NOT NULL COMMENT '供应商名称';",
"ALTER TABLE scm_supplier MODIFY COLUMN `supplier_type` TINYINT COMMENT '供应商类型：1生产型 2贸易型 3服务型';",
"ALTER TABLE scm_supplier MODIFY COLUMN `grade` TINYINT COMMENT '供应商等级：1A级 2AA级 3AAA级';",
"ALTER TABLE scm_supplier MODIFY COLUMN `material_category` TINYINT COMMENT '物资类别：1原材料 2辅料 3设备';",
"ALTER TABLE scm_supplier MODIFY COLUMN `cooperation_level` TINYINT COMMENT '合作分级：1战略 2合格 3潜在';",
"ALTER TABLE scm_supplier MODIFY COLUMN `contact_person` VARCHAR(50) COMMENT '联系人';",
"ALTER TABLE scm_supplier MODIFY COLUMN `contact_phone` VARCHAR(20) COMMENT '联系电话';",
"ALTER TABLE scm_supplier MODIFY COLUMN `contact_email` VARCHAR(100) COMMENT '联系邮箱';",
"ALTER TABLE scm_supplier MODIFY COLUMN `address` VARCHAR(500) COMMENT '详细地址';",
"ALTER TABLE scm_supplier MODIFY COLUMN `status` TINYINT COMMENT '状态：0待准入 1已准入 2已冻结 3已淘汰';",
"ALTER TABLE scm_supplier MODIFY COLUMN `audit_status` TINYINT COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝';",
"ALTER TABLE scm_supplier MODIFY COLUMN `audit_remark` VARCHAR(500) COMMENT '审核备注';",
"ALTER TABLE scm_supplier MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除';",
"ALTER TABLE scm_supplier MODIFY COLUMN `remark` VARCHAR(1000) COMMENT '备注';",
"ALTER TABLE scm_supplier MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人';",
"ALTER TABLE scm_supplier MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间';",
"ALTER TABLE scm_supplier MODIFY COLUMN `update_by` VARCHAR(50) COMMENT '更新人';",
"ALTER TABLE scm_supplier MODIFY COLUMN `update_time` DATETIME COMMENT '更新时间';",

"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `qualification_type` VARCHAR(50) COMMENT '资质类型：BUSINESS_LICENSE营业执照, TAX_REGISTRATION税务登记证, ORG_CODE组织机构代码证, PRODUCT_CERT产品认证, QUALITY_CERT质量认证, OTHER其他';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `qualification_name` VARCHAR(200) COMMENT '资质名称';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `certificate_no` VARCHAR(100) COMMENT '证书编号';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `issue_date` DATE COMMENT '发证日期';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `expiry_date` DATE COMMENT '有效期至';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `is_long_term` TINYINT COMMENT '是否长期有效：0否 1是';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `file_urls` TEXT COMMENT '附件URL列表，多个用逗号分隔';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `issuing_authority` VARCHAR(200) COMMENT '发证机关';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_status` TINYINT COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_remark` VARCHAR(1000) COMMENT '审核备注';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_by` VARCHAR(50) COMMENT '审核人';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_time` DATETIME COMMENT '审核时间';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `alert_status` TINYINT COMMENT '预警状态：0正常 1即将到期 2已过期';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `alert_sent` TINYINT COMMENT '是否已发送预警：0否 1是';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `remark` VARCHAR(1000) COMMENT '备注';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `update_by` VARCHAR(50) COMMENT '更新人';",
"ALTER TABLE scm_supplier_qualification MODIFY COLUMN `update_time` DATETIME COMMENT '更新时间';",

"ALTER TABLE scm_qualification_alert MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `qualification_id` BIGINT COMMENT '资质ID';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_type` TINYINT COMMENT '预警类型：1即将到期 2已过期';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_title` VARCHAR(200) COMMENT '预警标题';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_content` VARCHAR(1000) COMMENT '预警内容';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_date` DATE COMMENT '预警日期';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `days_before_expiry` INT COMMENT '到期前天数（即将到期预警时有效）';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `read_time` DATETIME COMMENT '阅读时间';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除';",
"ALTER TABLE scm_qualification_alert MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间';",

"ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键';",
"ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID';",
"ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `old_material_category` TINYINT COMMENT '原物资类别';",
"ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `new_material_category` TINYINT COMMENT '新物资类别';",
"ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `old_cooperation_level` TINYINT COMMENT '原合作分级';",
"ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `new_cooperation_level` TINYINT COMMENT '新合作分级';",
"ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `change_reason` VARCHAR(500) COMMENT '变更原因';",
"ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除';",
"ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人';",
"ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间';"
]

def generate_sql_file():
    sql_content = """-- 修复SCM模块表及字段中文注释乱码问题
-- 数据库: small_tweaks_scm
-- 执行方式: 
--   1. 使用Navicat/MySQL Workbench等工具打开执行
--   2. 或使用命令行: mysql -u root --default-character-set=utf8mb4 small_tweaks_scm < fix_comments_final.sql

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

"""
    for sql in FIX_SQLS:
        sql_content += sql + "\n"
    
    sql_content += """
-- 验证修复结果
SELECT '========== 表注释 ==========' AS info;
SELECT TABLE_NAME, TABLE_COMMENT 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 'scm_%' 
ORDER BY TABLE_NAME;
"""
    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_file = os.path.join(script_dir, 'fix_comments_final.sql')
    
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(sql_content)
    
    print(f"\n✓ SQL文件已生成: {output_file}")
    return output_file

if __name__ == "__main__":
    output_file = generate_sql_file()
    
    print("\n" + "=" * 60)
    print("  请使用以下方式执行SQL文件：")
    print("=" * 60)
    print(f"\n方式1: 使用Navicat/MySQL Workbench")
    print(f"  打开文件: {output_file}")
    print(f"  在数据库 small_tweaks_scm 中执行")
    
    print(f"\n方式2: 使用MySQL命令行")
    print(f'  & "D:\\laragon\\bin\\mysql\\mysql-8.4.3-winx64\\bin\\mysql.exe" -u root --default-character-set=utf8mb4 small_tweaks_scm < "{output_file}"')
    
    print("\n" + "=" * 60)
    print("  执行后请验证：")
    print("=" * 60)
    print("  1. 查看表注释是否为中文")
    print("  2. 查看字段注释是否为中文")
