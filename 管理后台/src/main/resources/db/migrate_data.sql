-- 数据迁移脚本：从 base_rbac 迁移到 small_tweaks_scm
-- 执行前请确保 small_tweaks_scm 数据库已存在且表结构已创建

-- 先清空目标表（按依赖关系反向顺序）
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE small_tweaks_scm.sys_role_api;
TRUNCATE TABLE small_tweaks_scm.sys_role_menu;
TRUNCATE TABLE small_tweaks_scm.sys_user_role;
TRUNCATE TABLE small_tweaks_scm.sys_login_log;
TRUNCATE TABLE small_tweaks_scm.sys_operation_log;
TRUNCATE TABLE small_tweaks_scm.sys_api_permission;
TRUNCATE TABLE small_tweaks_scm.sys_menu;
TRUNCATE TABLE small_tweaks_scm.sys_role;
TRUNCATE TABLE small_tweaks_scm.sys_user;

SET FOREIGN_KEY_CHECKS = 1;

-- 迁移基础表数据（按依赖顺序）

-- 1. 迁移角色表
INSERT INTO small_tweaks_scm.sys_role
SELECT * FROM base_rbac.sys_role;

-- 2. 迁移用户表
INSERT INTO small_tweaks_scm.sys_user
SELECT * FROM base_rbac.sys_user;

-- 3. 迁移菜单表
INSERT INTO small_tweaks_scm.sys_menu
SELECT * FROM base_rbac.sys_menu;

-- 4. 迁移API权限表
INSERT INTO small_tweaks_scm.sys_api_permission
SELECT * FROM base_rbac.sys_api_permission;

-- 迁移关联表数据

-- 5. 迁移用户角色关联表
INSERT INTO small_tweaks_scm.sys_user_role
SELECT * FROM base_rbac.sys_user_role;

-- 6. 迁移角色菜单关联表
INSERT INTO small_tweaks_scm.sys_role_menu
SELECT * FROM base_rbac.sys_role_menu;

-- 7. 迁移角色API关联表
INSERT INTO small_tweaks_scm.sys_role_api
SELECT * FROM base_rbac.sys_role_api;

-- 迁移日志表数据

-- 8. 迁移操作日志表
INSERT INTO small_tweaks_scm.sys_operation_log
SELECT * FROM base_rbac.sys_operation_log;

-- 9. 迁移登录日志表
INSERT INTO small_tweaks_scm.sys_login_log
SELECT * FROM base_rbac.sys_login_log;

-- 显示迁移结果
SELECT 
    'sys_role' as table_name, 
    (SELECT COUNT(*) FROM small_tweaks_scm.sys_role) as target_count,
    (SELECT COUNT(*) FROM base_rbac.sys_role) as source_count
UNION ALL
SELECT 
    'sys_user', 
    (SELECT COUNT(*) FROM small_tweaks_scm.sys_user),
    (SELECT COUNT(*) FROM base_rbac.sys_user)
UNION ALL
SELECT 
    'sys_menu', 
    (SELECT COUNT(*) FROM small_tweaks_scm.sys_menu),
    (SELECT COUNT(*) FROM base_rbac.sys_menu)
UNION ALL
SELECT 
    'sys_api_permission', 
    (SELECT COUNT(*) FROM small_tweaks_scm.sys_api_permission),
    (SELECT COUNT(*) FROM base_rbac.sys_api_permission)
UNION ALL
SELECT 
    'sys_user_role', 
    (SELECT COUNT(*) FROM small_tweaks_scm.sys_user_role),
    (SELECT COUNT(*) FROM base_rbac.sys_user_role)
UNION ALL
SELECT 
    'sys_role_menu', 
    (SELECT COUNT(*) FROM small_tweaks_scm.sys_role_menu),
    (SELECT COUNT(*) FROM base_rbac.sys_role_menu)
UNION ALL
SELECT 
    'sys_role_api', 
    (SELECT COUNT(*) FROM small_tweaks_scm.sys_role_api),
    (SELECT COUNT(*) FROM base_rbac.sys_role_api)
UNION ALL
SELECT 
    'sys_operation_log', 
    (SELECT COUNT(*) FROM small_tweaks_scm.sys_operation_log),
    (SELECT COUNT(*) FROM base_rbac.sys_operation_log)
UNION ALL
SELECT 
    'sys_login_log', 
    (SELECT COUNT(*) FROM small_tweaks_scm.sys_login_log),
    (SELECT COUNT(*) FROM base_rbac.sys_login_log);
