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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class ScmMenuInitializer implements CommandLineRunner {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Long ADMIN_ROLE_ID = 1L;

    @Override
    public void run(String... args) {
        log.info("开始初始化SCM菜单...");
        
        clearMenuCache();
        
        try {
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getPath, "/scm");
            Long count = menuMapper.selectCount(wrapper);
            
            if (count > 0) {
                log.info("SCM菜单已存在，跳过初始化（缓存已清除）");
                return;
            }
            
            Long parentId = createScmParentMenu();
            
            Long supplierMenuId = createSupplierMenu(parentId);
            createSupplierButtons(supplierMenuId);
            
            Long qualificationMenuId = createQualificationMenu(parentId);
            createQualificationButtons(qualificationMenuId);
            
            Long alertMenuId = createAlertMenu(parentId);
            createAlertButtons(alertMenuId);
            
            Long classificationMenuId = createClassificationMenu(parentId);
            createClassificationButtons(classificationMenuId);
            
            log.info("SCM菜单初始化完成");
            
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

    private Long createScmParentMenu() {
        SysMenu menu = new SysMenu();
        menu.setParentId(0L);
        menu.setMenuName("SCM管理");
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
        
        log.info("创建菜单: SCM管理, ID={}", menu.getId());
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
        menu.setSortOrder(3);
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
}
