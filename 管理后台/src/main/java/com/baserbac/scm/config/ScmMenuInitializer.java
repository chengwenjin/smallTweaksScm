package com.baserbac.scm.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baserbac.common.constant.RedisKeyConstant;
import com.baserbac.entity.SysMenu;
import com.baserbac.entity.SysRoleMenu;
import com.baserbac.mapper.MenuMapper;
import com.baserbac.mapper.RoleMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class ScmMenuInitializer implements CommandLineRunner {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    private static final Long ADMIN_ROLE_ID = 1L;

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("  开始初始化SCM菜单...");
        log.info("========================================");
        
        fixScmTableComments();
        
        clearMenuCache();
        deleteOldScmMenus();
        
        try {
            log.info("\n========== 创建SCM菜单层级 ==========");
            
            Long lifecycleMenuId = createLifecycleMenu();
            log.info("  [一级] 供应商全生命周期管理 (ID={})", lifecycleMenuId);
            
            Long accessMenuId = createAccessMenu(lifecycleMenuId);
            log.info("    [二级] 准入与分级管理 (ID={}, parentId={})", accessMenuId, lifecycleMenuId);
            
            Long supplierMenuId = createSupplierMenu(accessMenuId);
            log.info("      [三级] 供应商管理 (ID={}, parentId={})", supplierMenuId, accessMenuId);
            createSupplierButtons(supplierMenuId);
            
            Long qualificationMenuId = createQualificationMenu(accessMenuId);
            log.info("      [三级] 资质审核 (ID={}, parentId={})", qualificationMenuId, accessMenuId);
            createQualificationButtons(qualificationMenuId);
            
            Long alertMenuId = createAlertMenu(accessMenuId);
            log.info("      [三级] 预警管理 (ID={}, parentId={})", alertMenuId, accessMenuId);
            createAlertButtons(alertMenuId);
            
            Long classificationMenuId = createClassificationMenu(accessMenuId);
            log.info("      [三级] 分级分类 (ID={}, parentId={})", classificationMenuId, accessMenuId);
            createClassificationButtons(classificationMenuId);
            
            Long blacklistMenuId = createBlacklistMenu(accessMenuId);
            log.info("      [三级] 黑名单管理 (ID={}, parentId={})", blacklistMenuId, accessMenuId);
            createBlacklistButtons(blacklistMenuId);
            
            log.info("\n========================================");
            log.info("  SCM菜单初始化完成！");
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("SCM菜单初始化失败", e);
        }
    }

    private void clearMenuCache() {
        try {
            Set<String> menuKeys = redisTemplate.keys(RedisKeyConstant.MENUS_PREFIX + "*");
            Set<String> permsKeys = redisTemplate.keys(RedisKeyConstant.PERMISSIONS_PREFIX + "*");
            
            if (menuKeys != null && !menuKeys.isEmpty()) {
                redisTemplate.delete(menuKeys);
                log.info("清除菜单缓存: {} 条", menuKeys.size());
            }
            
            if (permsKeys != null && !permsKeys.isEmpty()) {
                redisTemplate.delete(permsKeys);
                log.info("清除权限缓存: {} 条", permsKeys.size());
            }
            
        } catch (Exception e) {
            log.warn("清除缓存失败", e);
        }
    }

    private void deleteOldScmMenus() {
        try {
            List<String> scmMenuNames = Arrays.asList(
                "供应商全生命周期管理",
                "准入与分级管理",
                "供应商管理",
                "资质审核",
                "预警管理",
                "分级分类",
                "黑名单管理"
            );
            
            LambdaQueryWrapper<SysMenu> scmWrapper = new LambdaQueryWrapper<>();
            scmWrapper.like(SysMenu::getPath, "/scm")
                    .or()
                    .in(SysMenu::getMenuName, scmMenuNames)
                    .or()
                    .like(SysMenu::getPermissionKey, "scm:");
            List<SysMenu> oldMenus = menuMapper.selectList(scmWrapper);
            
            if (oldMenus == null || oldMenus.isEmpty()) {
                log.info("未找到旧的SCM菜单，无需删除");
                return;
            }
            
            List<Long> menuIds = oldMenus.stream()
                    .map(SysMenu::getId)
                    .collect(Collectors.toList());
            
            log.info("准备删除旧的SCM菜单: {} 个", menuIds.size());
            for (SysMenu menu : oldMenus) {
                log.info("  - 删除菜单: {} (ID={}, path={})", menu.getMenuName(), menu.getId(), menu.getPath());
            }
            
            LambdaQueryWrapper<SysRoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
            roleMenuWrapper.in(SysRoleMenu::getMenuId, menuIds);
            int deletedRoleMenus = roleMenuMapper.delete(roleMenuWrapper);
            log.info("删除角色菜单关联: {} 条", deletedRoleMenus);
            
            for (Long menuId : menuIds) {
                menuMapper.deleteById(menuId);
            }
            log.info("删除旧SCM菜单完成: {} 个", menuIds.size());
            
        } catch (Exception e) {
            log.warn("删除旧SCM菜单失败", e);
        }
    }

    private Long createLifecycleMenu() {
        SysMenu menu = new SysMenu();
        menu.setParentId(0L);
        menu.setMenuName("供应商全生命周期管理");
        menu.setMenuType(1);
        menu.setPath("/scm");
        menu.setComponent("Layout");
        menu.setIcon("OfficeBuilding");
        menu.setSortOrder(70);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 供应商全生命周期管理, ID={}", menu.getId());
        return menu.getId();
    }

    private Long createAccessMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("准入与分级管理");
        menu.setMenuType(1);
        menu.setPath("access");
        menu.setComponent("Layout");
        menu.setIcon("Document");
        menu.setSortOrder(1);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 准入与分级管理, ID={}", menu.getId());
        return menu.getId();
    }

    private Long createSupplierMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("供应商管理");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:supplier:list");
        menu.setPath("supplier");
        menu.setComponent("scm/supplier/index");
        menu.setIcon("User");
        menu.setSortOrder(1);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 供应商管理, ID={}", menu.getId());
        return menu.getId();
    }

    private void createSupplierButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "新增供应商", "scm:supplier:add", 1));
        buttons.add(createButton(parentId, "编辑供应商", "scm:supplier:edit", 2));
        buttons.add(createButton(parentId, "删除供应商", "scm:supplier:delete", 3));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createQualificationMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("资质审核");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:qualification:list");
        menu.setPath("qualification");
        menu.setComponent("scm/qualification/index");
        menu.setIcon("DocumentChecked");
        menu.setSortOrder(2);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 资质审核, ID={}", menu.getId());
        return menu.getId();
    }

    private void createQualificationButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "新增资质", "scm:qualification:add", 1));
        buttons.add(createButton(parentId, "编辑资质", "scm:qualification:edit", 2));
        buttons.add(createButton(parentId, "删除资质", "scm:qualification:delete", 3));
        buttons.add(createButton(parentId, "审核资质", "scm:qualification:audit", 4));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createAlertMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("预警管理");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:alert:list");
        menu.setPath("alert");
        menu.setComponent("scm/alert/index");
        menu.setIcon("Warning");
        menu.setSortOrder(3);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 预警管理, ID={}", menu.getId());
        return menu.getId();
    }

    private void createAlertButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "标记已读", "scm:alert:read", 1));
        buttons.add(createButton(parentId, "全部已读", "scm:alert:readAll", 2));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createClassificationMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("分级分类");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:classification:list");
        menu.setPath("classification");
        menu.setComponent("scm/classification/index");
        menu.setIcon("Grid");
        menu.setSortOrder(4);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 分级分类, ID={}", menu.getId());
        return menu.getId();
    }

    private void createClassificationButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "批量设置", "scm:classification:set", 1));
        buttons.add(createButton(parentId, "查看记录", "scm:classification:logs", 2));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private SysMenu createButton(Long parentId, String name, String permission, int sort) {
        SysMenu btn = new SysMenu();
        btn.setParentId(parentId);
        btn.setMenuName(name);
        btn.setMenuType(3);
        btn.setPermissionKey(permission);
        btn.setSortOrder(sort);
        btn.setStatus(1);
        btn.setIsSystem(1);
        return btn;
    }

    private void assignToAdmin(Long menuId) {
        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setRoleId(ADMIN_ROLE_ID);
        roleMenu.setMenuId(menuId);
        roleMenuMapper.insert(roleMenu);
    }

    private void fixScmTableComments() {
        log.info("========================================");
        log.info("  修复SCM表及字段中文注释");
        log.info("========================================");

        try {
            jdbcTemplate.execute("SET NAMES utf8mb4");
            jdbcTemplate.execute("SET CHARACTER SET utf8mb4");

            log.info("\n========== 修复前的表注释 ==========");
            showTableComments();

            log.info("\n========== 开始执行修复 ==========");

            List<String> sqls = getFixSqls();
            int success = 0;
            int fail = 0;

            for (String sql : sqls) {
                try {
                    jdbcTemplate.execute(sql);
                    success++;
                } catch (Exception e) {
                    fail++;
                    log.warn("执行失败: {} - {}", sql.substring(0, Math.min(60, sql.length())), e.getMessage());
                }
            }

            log.info("\n========== 执行结果 ==========");
            log.info("成功: {} 条", success);
            log.info("失败: {} 条", fail);

            log.info("\n========== 修复后的表注释 ==========");
            showTableComments();

            log.info("\n========================================");
            log.info("  注释修复完成！");
            log.info("========================================");

        } catch (Exception e) {
            log.error("修复注释失败", e);
        }
    }

    private void showTableComments() {
        try {
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 'scm_%' ORDER BY TABLE_NAME"
            );
            for (Map<String, Object> table : tables) {
                log.info("{} | {}", table.get("TABLE_NAME"), table.get("TABLE_COMMENT"));
            }
        } catch (Exception e) {
            log.error("查询表注释失败", e);
        }
    }

    private List<String> getFixSqls() {
        List<String> sqls = new ArrayList<>();

        sqls.add("ALTER TABLE scm_supplier COMMENT = '供应商基础信息表'");
        sqls.add("ALTER TABLE scm_supplier_qualification COMMENT = '供应商资质审核表'");
        sqls.add("ALTER TABLE scm_qualification_alert COMMENT = '资质预警记录表'");
        sqls.add("ALTER TABLE scm_supplier_classification_log COMMENT = '供应商分级分类变更记录表'");

        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `supplier_code` VARCHAR(50) NOT NULL COMMENT '供应商编码'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `supplier_name` VARCHAR(200) NOT NULL COMMENT '供应商名称'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `supplier_type` TINYINT COMMENT '供应商类型'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `grade` TINYINT COMMENT '供应商等级'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `material_category` TINYINT COMMENT '物资类别'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `cooperation_level` TINYINT COMMENT '合作分级'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `contact_person` VARCHAR(50) COMMENT '联系人'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `contact_phone` VARCHAR(20) COMMENT '联系电话'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `contact_email` VARCHAR(100) COMMENT '联系邮箱'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `address` VARCHAR(500) COMMENT '详细地址'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `status` TINYINT COMMENT '状态'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `audit_status` TINYINT COMMENT '审核状态'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `audit_remark` VARCHAR(500) COMMENT '审核备注'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `remark` VARCHAR(1000) COMMENT '备注'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `update_by` VARCHAR(50) COMMENT '更新人'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `update_time` DATETIME COMMENT '更新时间'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `ext1` VARCHAR(255) COMMENT '预留扩展字段1'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `ext2` VARCHAR(255) COMMENT '预留扩展字段2'");
        sqls.add("ALTER TABLE scm_supplier MODIFY COLUMN `ext3` VARCHAR(255) COMMENT '预留扩展字段3'");

        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `qualification_type` VARCHAR(50) COMMENT '资质类型'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `qualification_name` VARCHAR(200) COMMENT '资质名称'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `certificate_no` VARCHAR(100) COMMENT '证书编号'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `issue_date` DATE COMMENT '发证日期'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `expiry_date` DATE COMMENT '有效期至'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `is_long_term` TINYINT COMMENT '是否长期有效'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `file_urls` TEXT COMMENT '附件URL列表'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `issuing_authority` VARCHAR(200) COMMENT '发证机关'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_status` TINYINT COMMENT '审核状态'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_remark` VARCHAR(1000) COMMENT '审核备注'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_by` VARCHAR(50) COMMENT '审核人'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `audit_time` DATETIME COMMENT '审核时间'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `alert_status` TINYINT COMMENT '预警状态'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `alert_sent` TINYINT COMMENT '是否已发送预警'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `remark` VARCHAR(1000) COMMENT '备注'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `update_by` VARCHAR(50) COMMENT '更新人'");
        sqls.add("ALTER TABLE scm_supplier_qualification MODIFY COLUMN `update_time` DATETIME COMMENT '更新时间'");

        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `qualification_id` BIGINT COMMENT '资质ID'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_type` TINYINT COMMENT '预警类型'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_title` VARCHAR(200) COMMENT '预警标题'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_content` VARCHAR(1000) COMMENT '预警内容'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `alert_date` DATE COMMENT '预警日期'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `days_before_expiry` INT COMMENT '到期前天数'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `is_read` TINYINT DEFAULT 0 COMMENT '是否已读'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `read_time` DATETIME COMMENT '阅读时间'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除'");
        sqls.add("ALTER TABLE scm_qualification_alert MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'");

        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `old_material_category` TINYINT COMMENT '原物资类别'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `new_material_category` TINYINT COMMENT '新物资类别'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `old_cooperation_level` TINYINT COMMENT '原合作分级'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `new_cooperation_level` TINYINT COMMENT '新合作分级'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `change_reason` VARCHAR(500) COMMENT '变更原因'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人'");
        sqls.add("ALTER TABLE scm_supplier_classification_log MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'");

        sqls.add("ALTER TABLE scm_supplier_blacklist COMMENT = '供应商黑名单表'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `supplier_id` BIGINT COMMENT '供应商ID'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `supplier_code` VARCHAR(50) COMMENT '供应商编码'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `supplier_name` VARCHAR(200) COMMENT '供应商名称'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `blacklist_type` TINYINT COMMENT '黑名单类型：1严重违约 2质量问题 3欺诈行为 4其他'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `blacklist_reason` VARCHAR(1000) COMMENT '列入原因'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `blacklist_date` DATE COMMENT '列入日期'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `is_permanent` TINYINT COMMENT '是否永久：0否 1是'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `expire_date` DATE COMMENT '到期日期'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `status` TINYINT COMMENT '状态：1在黑名单 2已移除'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `remove_reason` VARCHAR(1000) COMMENT '移除原因'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `remove_date` DATE COMMENT '移除日期'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `remark` VARCHAR(1000) COMMENT '备注'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `create_by` VARCHAR(50) COMMENT '创建人'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `create_time` DATETIME COMMENT '创建时间'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `update_by` VARCHAR(50) COMMENT '更新人'");
        sqls.add("ALTER TABLE scm_supplier_blacklist MODIFY COLUMN `update_time` DATETIME COMMENT '更新时间'");

        return sqls;
    }

    private Long createBlacklistMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("黑名单管理");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:blacklist:list");
        menu.setPath("blacklist");
        menu.setComponent("scm/blacklist/index");
        menu.setIcon("WarningFilled");
        menu.setSortOrder(5);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 黑名单管理, ID={}", menu.getId());
        return menu.getId();
    }

    private void createBlacklistButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "列入黑名单", "scm:blacklist:add", 1));
        buttons.add(createButton(parentId, "移除黑名单", "scm:blacklist:remove", 2));
        buttons.add(createButton(parentId, "查看详情", "scm:blacklist:view", 3));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }
}
