-- 初始化基础数据
USE `base_rbac`;

-- 1. 插入默认角色
INSERT INTO `sys_role` (`role_name`, `role_key`, `sort_order`, `status`, `is_system`, `remark`) VALUES
('超级管理员', 'admin', 1, 1, 1, '系统内置超级管理员'),
('普通用户', 'user', 2, 1, 0, '普通用户角色');

-- 2. 插入默认管理员用户 (密码: admin123, BCrypt加密)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `status`, `must_change_pwd`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800138000', 'admin@example.com', 1, 0);

-- 3. 关联管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 4. 插入顶级菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission_key`, `path`, `component`, `icon`, `sort_order`, `is_visible`, `status`, `is_system`) VALUES
(0, '系统管理', 1, NULL, '/system', 'Layout', 'Setting', 100, 1, 1, 1),
(0, '权限管理', 1, NULL, '/permission', 'Layout', 'Lock', 90, 1, 1, 1),
(0, '日志管理', 1, NULL, '/log', 'Layout', 'Document', 80, 1, 1, 1);

-- 5. 插入系统管理子菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission_key`, `path`, `component`, `icon`, `sort_order`, `is_visible`, `status`, `is_system`) VALUES
(1, '用户管理', 2, 'system:user:list', 'user', 'system/user/index', 'User', 1, 1, 1, 1),
(1, '角色管理', 2, 'system:role:list', 'role', 'system/role/index', 'UserFilled', 2, 1, 1, 1),
(1, '菜单管理', 2, 'system:menu:list', 'menu', 'system/menu/index', 'Menu', 3, 1, 1, 1);

-- 6. 插入用户管理按钮权限
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission_key`, `sort_order`, `status`, `is_system`) VALUES
(4, '新增用户', 3, 'system:user:add', 1, 1, 1),
(4, '编辑用户', 3, 'system:user:edit', 2, 1, 1),
(4, '删除用户', 3, 'system:user:delete', 3, 1, 1),
(4, '重置密码', 3, 'system:user:resetPwd', 4, 1, 1);

-- 7. 给超级管理员分配所有菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT 1, id FROM `sys_menu`;

-- 8. 插入示例接口权限
INSERT INTO `sys_api_permission` (`permission_key`, `api_name`, `module`, `request_method`, `api_path`, `description`, `status`) VALUES
('system:user:list', '查询用户列表', '用户管理', 'GET', '/api/system/users', '分页查询用户列表', 1),
('system:user:add', '新增用户', '用户管理', 'POST', '/api/system/users', '创建新用户', 1),
('system:user:edit', '编辑用户', '用户管理', 'PUT', '/api/system/users/{id}', '更新用户信息', 1),
('system:user:delete', '删除用户', '用户管理', 'DELETE', '/api/system/users/{id}', '删除指定用户', 1),
('system:role:list', '查询角色列表', '角色管理', 'GET', '/api/system/roles', '分页查询角色列表', 1);

-- 9. 给超级管理员分配所有接口权限
INSERT INTO `sys_role_api` (`role_id`, `api_id`) 
SELECT 1, id FROM `sys_api_permission`;
