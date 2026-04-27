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
            
            Long inquiryMenuId = createInquiryTenderMenu(lifecycleMenuId);
            log.info("    [二级] 询价与招投标 (ID={}, parentId={})", inquiryMenuId, lifecycleMenuId);
            
            Long requirementMenuId = createRequirementMenu(inquiryMenuId);
            log.info("      [三级] 采购需求单 (ID={}, parentId={})", requirementMenuId, inquiryMenuId);
            createRequirementButtons(requirementMenuId);
            
            Long inquiryDetailMenuId = createInquiryDetailMenu(inquiryMenuId);
            log.info("      [三级] 一键询价 (ID={}, parentId={})", inquiryDetailMenuId, inquiryMenuId);
            createInquiryDetailButtons(inquiryDetailMenuId);
            
            Long comparisonMenuId = createComparisonMenu(inquiryMenuId);
            log.info("      [三级] 智能比价 (ID={}, parentId={})", comparisonMenuId, inquiryMenuId);
            createComparisonButtons(comparisonMenuId);
            
            Long tenderMenuId = createTenderMenu(inquiryMenuId);
            log.info("      [三级] 招投标管理 (ID={}, parentId={})", tenderMenuId, inquiryMenuId);
            createTenderButtons(tenderMenuId);
            
            Long performanceMenuId = createPerformanceMenu(lifecycleMenuId);
            log.info("    [二级] 绩效考核体系 (ID={}, parentId={})", performanceMenuId, lifecycleMenuId);
            
            Long kpiMenuId = createKpiMenu(performanceMenuId);
            log.info("      [三级] 多维考核 (ID={}, parentId={})", kpiMenuId, performanceMenuId);
            createKpiButtons(kpiMenuId);
            
            Long reportMenuId = createReportMenu(performanceMenuId);
            log.info("      [三级] 考核报告 (ID={}, parentId={})", reportMenuId, performanceMenuId);
            createReportButtons(reportMenuId);
            
            Long collaborationMenuId = createCollaborationMenu(lifecycleMenuId);
            log.info("    [二级] 采购全流程协同管理 (ID={}, parentId={})", collaborationMenuId, lifecycleMenuId);
            
            Long purchaseReqMenuId = createPurchaseReqPlanMenu(collaborationMenuId);
            log.info("      [三级] 采购需求与计划 (ID={}, parentId={})", purchaseReqMenuId, collaborationMenuId);
            
            Long demandSummaryMenuId = createDemandSummaryMenu(purchaseReqMenuId);
            log.info("        [四级] 需求汇总 (ID={}, parentId={})", demandSummaryMenuId, purchaseReqMenuId);
            createDemandSummaryButtons(demandSummaryMenuId);
            
            Long replenishmentMenuId = createReplenishmentMenu(purchaseReqMenuId);
            log.info("        [四级] 智能补货 (ID={}, parentId={})", replenishmentMenuId, purchaseReqMenuId);
            createReplenishmentButtons(replenishmentMenuId);
            
            Long approvalMenuId = createApprovalMenu(purchaseReqMenuId);
            log.info("        [四级] 审批联动 (ID={}, parentId={})", approvalMenuId, purchaseReqMenuId);
            createApprovalButtons(approvalMenuId);
            
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
                "黑名单管理",
                "询价与招投标",
                "采购需求单",
                "一键询价",
                "智能比价",
                "招投标管理",
                "采购全流程协同管理",
                "采购需求与计划",
                "需求汇总",
                "智能补货",
                "审批联动"
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

    private Long createInquiryTenderMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("询价与招投标");
        menu.setMenuType(1);
        menu.setPath("inquiry");
        menu.setComponent("Layout");
        menu.setIcon("Document");
        menu.setSortOrder(2);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 询价与招投标, ID={}", menu.getId());
        return menu.getId();
    }

    private Long createRequirementMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("采购需求单");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:requirement:list");
        menu.setPath("requirement");
        menu.setComponent("scm/requirement/index");
        menu.setIcon("Document");
        menu.setSortOrder(1);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 采购需求单, ID={}", menu.getId());
        return menu.getId();
    }

    private void createRequirementButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "新增需求单", "scm:requirement:add", 1));
        buttons.add(createButton(parentId, "编辑需求单", "scm:requirement:edit", 2));
        buttons.add(createButton(parentId, "删除需求单", "scm:requirement:delete", 3));
        buttons.add(createButton(parentId, "发起询价", "scm:requirement:inquiry", 4));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createInquiryDetailMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("一键询价");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:inquiry:list");
        menu.setPath("inquiry-detail");
        menu.setComponent("scm/inquiry/index");
        menu.setIcon("Share");
        menu.setSortOrder(2);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 一键询价, ID={}", menu.getId());
        return menu.getId();
    }

    private void createInquiryDetailButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "创建询价单", "scm:inquiry:add", 1));
        buttons.add(createButton(parentId, "发布询价", "scm:inquiry:publish", 2));
        buttons.add(createButton(parentId, "查看报价", "scm:inquiry:quote", 3));
        buttons.add(createButton(parentId, "取消询价", "scm:inquiry:cancel", 4));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createComparisonMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("智能比价");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:comparison:list");
        menu.setPath("comparison");
        menu.setComponent("scm/comparison/index");
        menu.setIcon("DataAnalysis");
        menu.setSortOrder(3);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 智能比价, ID={}", menu.getId());
        return menu.getId();
    }

    private void createComparisonButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "生成比价单", "scm:comparison:generate", 1));
        buttons.add(createButton(parentId, "查看比价", "scm:comparison:view", 2));
        buttons.add(createButton(parentId, "确认推荐", "scm:comparison:confirm", 3));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createTenderMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("招投标管理");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:tender:list");
        menu.setPath("tender");
        menu.setComponent("scm/tender/index");
        menu.setIcon("Coin");
        menu.setSortOrder(4);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 招投标管理, ID={}", menu.getId());
        return menu.getId();
    }

    private void createTenderButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "创建招标", "scm:tender:add", 1));
        buttons.add(createButton(parentId, "发布招标", "scm:tender:publish", 2));
        buttons.add(createButton(parentId, "查看投标", "scm:tender:bid", 3));
        buttons.add(createButton(parentId, "开标", "scm:tender:open", 4));
        buttons.add(createButton(parentId, "定标", "scm:tender:award", 5));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createPerformanceMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("绩效考核体系");
        menu.setMenuType(1);
        menu.setPath("performance");
        menu.setComponent("Layout");
        menu.setIcon("TrendCharts");
        menu.setSortOrder(3);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 绩效考核体系, ID={}", menu.getId());
        return menu.getId();
    }

    private Long createKpiMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("多维考核");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:kpi:list");
        menu.setPath("kpi");
        menu.setComponent("scm/kpi/index");
        menu.setIcon("DataLine");
        menu.setSortOrder(1);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 多维考核, ID={}", menu.getId());
        return menu.getId();
    }

    private void createKpiButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "计算KPI", "scm:kpi:calculate", 1));
        buttons.add(createButton(parentId, "查看详情", "scm:kpi:view", 2));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createReportMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("考核报告");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:report:list");
        menu.setPath("report");
        menu.setComponent("scm/report/index");
        menu.setIcon("DocumentCopy");
        menu.setSortOrder(2);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 考核报告, ID={}", menu.getId());
        return menu.getId();
    }

    private void createReportButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "生成报告", "scm:report:generate", 1));
        buttons.add(createButton(parentId, "查看报告", "scm:report:view", 2));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createCollaborationMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("采购全流程协同管理");
        menu.setMenuType(1);
        menu.setPath("collaboration");
        menu.setComponent("Layout");
        menu.setIcon("Connection");
        menu.setSortOrder(4);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 采购全流程协同管理, ID={}", menu.getId());
        return menu.getId();
    }

    private Long createPurchaseReqPlanMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("采购需求与计划");
        menu.setMenuType(1);
        menu.setPath("purchase-req-plan");
        menu.setComponent("Layout");
        menu.setIcon("ShoppingCart");
        menu.setSortOrder(1);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 采购需求与计划, ID={}", menu.getId());
        return menu.getId();
    }

    private Long createDemandSummaryMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("需求汇总");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:demand-summary:list");
        menu.setPath("demand-summary");
        menu.setComponent("scm/demand-summary/index");
        menu.setIcon("Collection");
        menu.setSortOrder(1);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 需求汇总, ID={}", menu.getId());
        return menu.getId();
    }

    private void createDemandSummaryButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "新增申请", "scm:demand-summary:add", 1));
        buttons.add(createButton(parentId, "生成汇总", "scm:demand-summary:generate", 2));
        buttons.add(createButton(parentId, "查看详情", "scm:demand-summary:view", 3));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createReplenishmentMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("智能补货");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:replenishment:list");
        menu.setPath("replenishment");
        menu.setComponent("scm/replenishment/index");
        menu.setIcon("Cpu");
        menu.setSortOrder(2);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 智能补货, ID={}", menu.getId());
        return menu.getId();
    }

    private void createReplenishmentButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "生成补货计划", "scm:replenishment:generate", 1));
        buttons.add(createButton(parentId, "查看计划", "scm:replenishment:view", 2));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }

    private Long createApprovalMenu(Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName("审批联动");
        menu.setMenuType(2);
        menu.setPermissionKey("scm:approval:list");
        menu.setPath("approval");
        menu.setComponent("scm/approval/index");
        menu.setIcon("Stamp");
        menu.setSortOrder(3);
        menu.setIsVisible(1);
        menu.setStatus(1);
        menu.setIsSystem(1);
        menuMapper.insert(menu);
        
        assignToAdmin(menu.getId());
        
        log.info("创建菜单: 审批联动, ID={}", menu.getId());
        return menu.getId();
    }

    private void createApprovalButtons(Long parentId) {
        List<SysMenu> buttons = new ArrayList<>();
        
        buttons.add(createButton(parentId, "提交审批", "scm:approval:submit", 1));
        buttons.add(createButton(parentId, "处理审批", "scm:approval:process", 2));
        buttons.add(createButton(parentId, "撤回审批", "scm:approval:withdraw", 3));
        buttons.add(createButton(parentId, "查看详情", "scm:approval:view", 4));
        
        for (SysMenu btn : buttons) {
            menuMapper.insert(btn);
            assignToAdmin(btn.getId());
            log.info("创建按钮: {}, ID={}", btn.getMenuName(), btn.getId());
        }
    }
}
