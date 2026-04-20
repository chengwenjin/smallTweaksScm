CREATE DATABASE IF NOT EXISTS `base_rbac` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `base_rbac`;

CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `real_name` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `avatar` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `must_change_pwd` TINYINT NOT NULL DEFAULT 0,
    `login_fail_count` INT NOT NULL DEFAULT 0,
    `lock_time` DATETIME DEFAULT NULL,
    `last_login_time` DATETIME DEFAULT NULL,
    `last_login_ip` VARCHAR(50) DEFAULT NULL,
    `remark` VARCHAR(255) DEFAULT NULL,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `ext1` VARCHAR(255) DEFAULT NULL,
    `ext2` VARCHAR(255) DEFAULT NULL,
    `ext3` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_name` VARCHAR(50) NOT NULL,
    `role_key` VARCHAR(50) NOT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `is_system` TINYINT NOT NULL DEFAULT 0,
    `remark` VARCHAR(255) DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `parent_id` BIGINT NOT NULL DEFAULT 0,
    `menu_name` VARCHAR(50) NOT NULL,
    `menu_type` TINYINT NOT NULL,
    `permission_key` VARCHAR(100) DEFAULT NULL,
    `path` VARCHAR(200) DEFAULT NULL,
    `component` VARCHAR(200) DEFAULT NULL,
    `icon` VARCHAR(50) DEFAULT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `is_visible` TINYINT NOT NULL DEFAULT 1,
    `is_cached` TINYINT NOT NULL DEFAULT 0,
    `is_external` TINYINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `is_system` TINYINT NOT NULL DEFAULT 0,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_key` (`permission_key`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_api_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `permission_key` VARCHAR(100) NOT NULL,
    `api_name` VARCHAR(100) NOT NULL,
    `module` VARCHAR(50) NOT NULL,
    `request_method` VARCHAR(10) NOT NULL,
    `api_path` VARCHAR(200) NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_by` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_key` (`permission_key`),
    KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `log_type` TINYINT NOT NULL,
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
    `status` TINYINT NOT NULL,
    `error_msg` TEXT,
    `duration` INT DEFAULT NULL,
    `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_operator` (`operator_id`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `login_status` TINYINT NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL,
    `menu_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_role_api` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL,
    `api_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_api` (`role_id`, `api_id`),
    KEY `idx_api_id` (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
