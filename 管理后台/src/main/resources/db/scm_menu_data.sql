-- 添加SCM管理菜单数据
-- 数据库: small_tweaks_scm

-- 1. 一级菜单：SCM管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission_key`, `path`, `component`, `icon`, `sort_order`, `is_visible`, `status`, `is_system`) VALUES
(0, 'SCM管理', 1, NULL, '/scm', 'Layout', 'OfficeBuilding', 70, 1, 1, 1);

-- 获取新插入的一级菜单ID（假设它是11）
-- 2. 二级菜单：供应商管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission_key`, `path`, `component`, `icon`, `sort_order`, `is_visible`, `status`, `is_system`) VALUES
(11, '供应商管理', 2, 'scm:supplier:list', 'supplier', 'scm/supplier/index', 'User', 1, 1, 1, 1);

-- 3. 二级菜单：资质审核
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission_key`, `path`, `component`, `icon`, `sort_order`, `is_visible`, `status`, `is_system`) VALUES
(11, '资质审核', 2, 'scm:qualification:list', 'qualification', 'scm/qualification/index', 'DocumentChecked', 2, 1, 1, 1);

-- 4. 二级菜单：预警管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission_key`, `path`, `component`, `icon`, `sort_order`, `is_visible`, `status`, `is_system`) VALUES
(11, '预警管理', 2, 'scm:alert:list', 'alert', 'scm/alert/index', 'Warning', 3, 1, 1, 1);

-- 5. 按钮权限：供应商管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission_key`, `sort_order`, `status`, `is_system`) VALUES
(12, '新增供应商', 3, 'scm:supplier:add', 1, 1, 1),
(12, '编辑供应商', 3, 'scm:supplier:edit', 2, 1, 1),
(12, '删除供应商', 3, 'scm:supplier:delete', 3, 1, 1);

-- 6. 按钮权限：资质审核
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission_key`, `sort_order`, `status`, `is_system`) VALUES
(13, '新增资质', 3, 'scm:qualification:add', 1, 1, 1),
(13, '编辑资质', 3, 'scm:qualification:edit', 2, 1, 1),
(13, '删除资质', 3, 'scm:qualification:delete', 3, 1, 1),
(13, '审核资质', 3, 'scm:qualification:audit', 4, 1, 1);

-- 7. 按钮权限：预警管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission_key`, `sort_order`, `status`, `is_system`) VALUES
(14, '标记已读', 3, 'scm:alert:read', 1, 1, 1),
(14, '全部已读', 3, 'scm:alert:readAll', 2, 1, 1);

-- 8. 分配菜单权限给管理员角色（role_id = 1）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT 1, id FROM `sys_menu` WHERE id >= 11;

-- 9. 添加API权限
INSERT INTO `sys_api_permission` (`permission_key`, `api_name`, `module`, `request_method`, `api_path`, `description`, `status`) VALUES
('scm:supplier:list', 'Query Suppliers', 'SCM Supplier', 'GET', '/api/scm/suppliers', 'Query supplier list', 1),
('scm:supplier:add', 'Add Supplier', 'SCM Supplier', 'POST', '/api/scm/suppliers', 'Create new supplier', 1),
('scm:supplier:edit', 'Edit Supplier', 'SCM Supplier', 'PUT', '/api/scm/suppliers/{id}', 'Update supplier info', 1),
('scm:supplier:delete', 'Delete Supplier', 'SCM Supplier', 'DELETE', '/api/scm/suppliers/{id}', 'Delete supplier', 1),
('scm:qualification:list', 'Query Qualifications', 'SCM Qualification', 'GET', '/api/scm/qualifications', 'Query qualification list', 1),
('scm:qualification:add', 'Add Qualification', 'SCM Qualification', 'POST', '/api/scm/qualifications', 'Create new qualification', 1),
('scm:qualification:edit', 'Edit Qualification', 'SCM Qualification', 'PUT', '/api/scm/qualifications/{id}', 'Update qualification info', 1),
('scm:qualification:delete', 'Delete Qualification', 'SCM Qualification', 'DELETE', '/api/scm/qualifications/{id}', 'Delete qualification', 1),
('scm:qualification:audit', 'Audit Qualification', 'SCM Qualification', 'POST', '/api/scm/qualifications/{id}/audit', 'Audit qualification', 1),
('scm:alert:list', 'Query Alerts', 'SCM Alert', 'GET', '/api/scm/alerts', 'Query alert list', 1);

-- 10. 分配API权限给管理员角色
INSERT INTO `sys_role_api` (`role_id`, `api_id`) 
SELECT 1, id FROM `sys_api_permission` WHERE `permission_key` LIKE 'scm:%';
