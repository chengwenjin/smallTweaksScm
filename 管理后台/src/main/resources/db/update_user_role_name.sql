-- 更新用户和角色数据为中文

-- 更新用户真实姓名
UPDATE `sys_user` SET `real_name` = '系统管理员' WHERE `id` = 1;

-- 更新角色名称和备注
UPDATE `sys_role` SET `role_name` = '管理员', `remark` = '系统管理员' WHERE `id` = 1;
UPDATE `sys_role` SET `role_name` = '普通用户', `remark` = '普通用户' WHERE `id` = 2;

-- 执行后请刷新前端页面查看效果
