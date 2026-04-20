-- 创建数据库
CREATE DATABASE IF NOT EXISTS `base_rbac` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `base_rbac`;

-- 1. sys_user 用户表
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名，全局唯一',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除',
    `must_change_pwd` TINYINT NOT NULL DEFAULT 0 COMMENT '是否强制修改密码',
    `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    `lock_time` DATETIME DEFAULT NULL COMMENT '账号锁定截止时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `ext1` VARCHAR(255) DEFAULT NULL,
    `ext2` VARCHAR(255) DEFAULT NULL,
    `ext3` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. sys_role 角色表
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_key` VARCHAR(50) NOT NULL COMMENT '角色标识',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_by` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `ext1` VARCHAR(255) DEFAULT NULL,
    `ext2` VARCHAR(255) DEFAULT NULL,
    `ext3` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_key` (`role_key`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. sys_menu 菜单表
CREATE TABLE `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID',
    `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `menu_type` TINYINT NOT NULL COMMENT '类型：1目录 2菜单 3按钮',
    `permission_key` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
    `sort_order` INT NOT NULL DEFAULT 0,
    `is_visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示',
    `is_cached` TINYINT NOT NULL DEFAULT 0 COMMENT '是否缓存',
    `is_external` TINYINT NOT NULL DEFAULT 0 COMMENT '是否外链',
    `status` TINYINT NOT NULL DEFAULT 1,
    `is_system` TINYINT NOT NULL DEFAULT 0,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_key` (`permission_key`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 4. sys_api_permission 接口权限表
CREATE TABLE `sys_api_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `permission_key` VARCHAR(100) NOT NULL COMMENT '权限标识',
    `api_name` VARCHAR(100) NOT NULL COMMENT '接口名称',
    `module` VARCHAR(50) NOT NULL COMMENT '所属模块',
    `request_method` VARCHAR(10) NOT NULL COMMENT '请求方式',
    `api_path` VARCHAR(200) NOT NULL COMMENT '接口路径',
    `description` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_key` (`permission_key`),
    KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口权限表';

-- 5. sys_operation_log 操作日志表
CREATE TABLE `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `log_type` TINYINT NOT NULL COMMENT '1登录 2操作 3异常',
    `operator_id` BIGINT NOT NULL,
    `operator_name` VARCHAR(50) NOT NULL,
    `module` VARCHAR(50) NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `request_method` VARCHAR(10) DEFAULT NULL,
    `request_url` VARCHAR(200) DEFAULT NULL,
    `request_params` TEXT,
    `request_headers` TEXT,
    `response_status` INT DEFAULT NULL,
    `response_result` TEXT,
    `ip` VARCHAR(50) DEFAULT NULL,
    `user_agent` VARCHAR(500) DEFAULT NULL,
    `status` TINYINT NOT NULL COMMENT '1成功 0失败',
    `error_msg` TEXT,
    `duration` INT DEFAULT NULL,
    `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_operator` (`operator_id`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 6. sys_login_log 登录日志表
CREATE TABLE `sys_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `login_status` TINYINT NOT NULL COMMENT '1成功 0失败',
    `fail_reason` VARCHAR(200) DEFAULT NULL,
    `ip` VARCHAR(50) DEFAULT NULL,
    `location` VARCHAR(100) DEFAULT NULL,
    `browser` VARCHAR(100) DEFAULT NULL,
    `os` VARCHAR(100) DEFAULT NULL,
    `user_agent` VARCHAR(500) DEFAULT NULL,
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 7. sys_user_role 用户-角色关联表
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- 8. sys_role_menu 角色-菜单关联表
CREATE TABLE `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL,
    `menu_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联表';

-- 9. sys_role_api 角色-接口权限关联表
CREATE TABLE `sys_role_api` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL,
    `api_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_api` (`role_id`, `api_id`),
    KEY `idx_api_id` (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-接口权限关联表';
