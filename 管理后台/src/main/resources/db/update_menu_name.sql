-- 更新菜单名称为中文
-- 一级菜单
UPDATE `sys_menu` SET `menu_name` = '系统管理' WHERE `id` = 1;
UPDATE `sys_menu` SET `menu_name` = '权限管理' WHERE `id` = 2;
UPDATE `sys_menu` SET `menu_name` = '日志管理' WHERE `id` = 3;

-- 二级菜单
UPDATE `sys_menu` SET `menu_name` = '用户管理' WHERE `id` = 4;
UPDATE `sys_menu` SET `menu_name` = '角色管理' WHERE `id` = 5;
UPDATE `sys_menu` SET `menu_name` = '菜单管理' WHERE `id` = 6;

-- 按钮权限
UPDATE `sys_menu` SET `menu_name` = '新增用户' WHERE `id` = 7;
UPDATE `sys_menu` SET `menu_name` = '编辑用户' WHERE `id` = 8;
UPDATE `sys_menu` SET `menu_name` = '删除用户' WHERE `id` = 9;
UPDATE `sys_menu` SET `menu_name` = '重置密码' WHERE `id` = 10;
