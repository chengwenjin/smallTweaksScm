#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复SCM模块表及字段中文注释乱码问题
使用MySQL Connector/Python执行
"""

import pymysql
from pymysql.cursors import DictCursor

DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '',
    'database': 'small_tweaks_scm',
    'charset': 'utf8mb4',
    'use_unicode': True
}

def get_fix_sqls():
    sqls = []
    
    sqls.append("ALTER TABLE scm_supplier COMMENT = '供应商基础信息表'")
    sqls.append("ALTER TABLE scm_supplier_qualification COMMENT = '供应商资质审核表'")
    sqls.append("ALTER TABLE scm_qualification_alert COMMENT = '资质预警记录表'")
    sqls.append("ALTER TABLE scm_supplier_classification_log COMMENT = '供应商分级分类变更记录表'")
    
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `supplier_code` VARCHAR(50) NOT NULL COMMENT '供应商编码'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `supplier_name` VARCHAR(200) NOT NULL COMMENT '供应商名称'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `supplier_type` TINYINT COMMENT '供应商类型：1生产型 2贸易型 3服务型'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `grade` TINYINT COMMENT '供应商等级：1A级 2AA级 3AAA级'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `material_category` TINYINT COMMENT '物资类别：1原材料 2辅料 3设备'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `cooperation_level` TINYINT COMMENT '合作分级：1战略 2合格 3潜在'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `contact_person` VARCHAR(50) COMMENT '联系人'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `contact_phone` VARCHAR(20) COMMENT '联系电话'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `contact_email` VARCHAR(100) COMMENT '联系邮箱'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `address` VARCHAR(500) COMMENT '详细地址'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `status` TINYINT COMMENT '状态：0待准入 1已准入 2已冻结 3已淘汰'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `audit_status` TINYINT COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `audit_remark` VARCHAR(500) COMMENT '审核备注'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `remark` VARCHAR(1000) COMMENT '备注'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `update_by` VARCHAR(50) COMMENT '更新人'")
    sqls.append("ALTER TABLE scm_supplier MODIFY COLUMN `update_time` DATETIME COMMENT '更新时间'")
    
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `qualification_type` VARCHAR(50) COMMENT '资质类型：BUSINESS_LICENSE营业执照, TAX_REGISTRATION税务登记证, ORG_CODE组织机构代码证, PRODUCT_CERT产品认证, QUALITY_CERT质量认证, OTHER其他'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `qualification_name` VARCHAR(200) COMMENT '资质名称'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `certificate_no` VARCHAR(100) COMMENT '证书编号'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `issue_date` DATE COMMENT '发证日期'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `expiry_date` DATE COMMENT '有效期至'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `is_long_term` TINYINT COMMENT '是否长期有效：0否 1是'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `file_urls` TEXT COMMENT '附件URL列表，多个用逗号分隔'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `issuing_authority` VARCHAR(200) COMMENT '发证机关'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_status` TINYINT COMMENT '审核状态：0待审核 1审核中 2审核通过 3审核拒绝'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_remark` VARCHAR(1000) COMMENT '审核备注'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_by` VARCHAR(50) COMMENT '审核人'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_time` DATETIME COMMENT '审核时间'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `alert_status` TINYINT COMMENT '预警状态：0正常 1即将到期 2已过期'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `alert_sent` TINYINT COMMENT '是否已发送预警：0否 1是'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `remark` VARCHAR(1000) COMMENT '备注'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `update_by` VARCHAR(50) COMMENT '更新人'")
    sqls.append("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `update_time` DATETIME COMMENT '更新时间'")
    
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `qualification_id` BIGINT COMMENT '资质ID'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_type` TINYINT COMMENT '预警类型：1即将到期 2已过期'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_title` VARCHAR(200) COMMENT '预警标题'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_content` VARCHAR(1000) COMMENT '预警内容'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_date` DATE COMMENT '预警日期'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `days_before_expiry` INT COMMENT '到期前天数（即将到期预警时有效）'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `read_time` DATETIME COMMENT '阅读时间'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'")
    sqls.append("ALTER TABLE scm_qualification_alert MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'")
    
    sqls.append("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'")
    sqls.append("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID'")
    sqls.append("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `old_material_category` TINYINT COMMENT '原物资类别'")
    sqls.append("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `new_material_category` TINYINT COMMENT '新物资类别'")
    sqls.append("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `old_cooperation_level` TINYINT COMMENT '原合作分级'")
    sqls.append("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `new_cooperation_level` TINYINT COMMENT '新合作分级'")
    sqls.append("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `change_reason` VARCHAR(500) COMMENT '变更原因'")
    sqls.append("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除：0未删除 1已删除'")
    sqls.append("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人'")
    sqls.append("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'")
    
    return sqls

def fix_comments():
    print("=" * 50)
    print("  修复SCM数据库中文注释乱码")
    print("=" * 50)
    
    try:
        connection = pymysql.connect(**DB_CONFIG)
        print("\n数据库连接成功！")
        print(f"数据库字符集: {connection.charset}")
        
        with connection.cursor() as cursor:
            cursor.execute("SET NAMES utf8mb4")
            cursor.execute("SET CHARACTER SET utf8mb4")
            print("设置字符集: utf8mb4")
            
            print("\n开始执行修复SQL...")
            print("-" * 50)
            
            sqls = get_fix_sqls()
            success_count = 0
            fail_count = 0
            failed_sqls = []
            
            for sql in sqls:
                try:
                    cursor.execute(sql)
                    success_count += 1
                    display_sql = sql[:60] + "..." if len(sql) > 60 else sql
                    print(f"✓ 成功: {display_sql}")
                except Exception as e:
                    fail_count += 1
                    failed_sqls.append(sql)
                    display_sql = sql[:60] + "..." if len(sql) > 60 else sql
                    print(f"✗ 失败: {display_sql}")
                    print(f"  错误: {e}")
            
            connection.commit()
            print("-" * 50)
            print(f"\n执行完成！")
            print(f"成功: {success_count} 条")
            print(f"失败: {fail_count} 条")
            
            if failed_sqls:
                print("\n失败的SQL:")
                for sql in failed_sqls:
                    print(f"  - {sql[:80]}...")
            
            print("\n" + "=" * 50)
            print("  验证修复结果")
            print("=" * 50)
            
            print("\n========== 表注释 ==========")
            cursor.execute("""
                SELECT TABLE_NAME, TABLE_COMMENT 
                FROM information_schema.TABLES 
                WHERE TABLE_SCHEMA = DATABASE() 
                AND TABLE_NAME LIKE 'scm_%' 
                ORDER BY TABLE_NAME
            """)
            tables = cursor.fetchall()
            print(f"\n{'表名':<45} | 表注释")
            print("-" * 80)
            for table in tables:
                print(f"{table[0]:<45} | {table[1]}")
            
            print("\n========== 字段注释 ==========")
            cursor.execute("""
                SELECT TABLE_NAME, COLUMN_NAME, COLUMN_COMMENT 
                FROM information_schema.COLUMNS 
                WHERE TABLE_SCHEMA = DATABASE() 
                AND TABLE_NAME LIKE 'scm_%' 
                ORDER BY TABLE_NAME, ORDINAL_POSITION
            """)
            columns = cursor.fetchall()
            
            current_table = ""
            for col in columns:
                table_name = col[0]
                column_name = col[1]
                column_comment = col[2]
                
                if table_name != current_table:
                    current_table = table_name
                    print(f"\n【{table_name}】")
                    print(f"  {'字段名':<25} | 字段注释")
                    print("  " + "-" * 55)
                
                print(f"  {column_name:<25} | {column_comment}")
            
            print("\n" + "=" * 50)
            print("  验证完成！")
            print("=" * 50)
        
        connection.close()
        
    except Exception as e:
        print(f"\n错误: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    fix_comments()
