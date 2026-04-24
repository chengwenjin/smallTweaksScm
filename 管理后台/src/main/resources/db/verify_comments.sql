-- 验证SCM表及字段中文注释

SELECT '========== 表注释 ==========' AS info;

SELECT 
    TABLE_NAME AS '表名',
    TABLE_COMMENT AS '表注释'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'small_tweaks_scm' 
  AND TABLE_NAME LIKE 'scm_%'
ORDER BY TABLE_NAME;

SELECT '========== 字段注释 ==========' AS info;

SELECT 
    TABLE_NAME AS '表名',
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '字段类型',
    COLUMN_COMMENT AS '字段注释'
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'small_tweaks_scm' 
  AND TABLE_NAME LIKE 'scm_%'
ORDER BY TABLE_NAME, ORDINAL_POSITION;
