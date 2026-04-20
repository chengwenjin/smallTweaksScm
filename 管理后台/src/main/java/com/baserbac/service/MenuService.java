package com.baserbac.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.dto.MenuCreateDTO;
import com.baserbac.dto.MenuQueryDTO;
import com.baserbac.dto.MenuUpdateDTO;
import com.baserbac.entity.SysMenu;
import com.baserbac.mapper.MenuMapper;
import com.baserbac.vo.MenuTreeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;

    /**
     * 获取菜单树
     */
    public List<MenuTreeVO> getMenuTree(MenuQueryDTO queryDTO) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        
        // 查询条件
        wrapper.like(queryDTO.getMenuName() != null, SysMenu::getMenuName, queryDTO.getMenuName())
               .eq(queryDTO.getMenuType() != null, SysMenu::getMenuType, queryDTO.getMenuType())
               .eq(queryDTO.getStatus() != null, SysMenu::getStatus, queryDTO.getStatus())
               .orderByAsc(SysMenu::getSortOrder);
        
        List<SysMenu> menus = menuMapper.selectList(wrapper);
        
        // 转换为VO并构建树形结构
        List<MenuTreeVO> menuVOs = menus.stream().map(this::convertToTreeVO).collect(Collectors.toList());
        
        return buildTree(menuVOs, 0L);
    }

    /**
     * 根据ID查询菜单
     */
    public MenuTreeVO getMenuById(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToTreeVO(menu);
    }

    /**
     * 创建菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void createMenu(MenuCreateDTO createDTO) {
        // 检查权限标识唯一性
        if (createDTO.getPermissionKey() != null && !createDTO.getPermissionKey().isEmpty()) {
            Long count = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getPermissionKey, createDTO.getPermissionKey())
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.PERMISSION_KEY_EXISTS);
            }
        }

        SysMenu menu = new SysMenu();
        menu.setParentId(createDTO.getParentId());
        menu.setMenuName(createDTO.getMenuName());
        menu.setMenuType(createDTO.getMenuType());
        menu.setPermissionKey(createDTO.getPermissionKey());
        menu.setPath(createDTO.getPath());
        menu.setComponent(createDTO.getComponent());
        menu.setIcon(createDTO.getIcon());
        menu.setSortOrder(createDTO.getSortOrder() != null ? createDTO.getSortOrder() : 0);
        menu.setIsVisible(createDTO.getIsVisible() != null ? createDTO.getIsVisible() : 1);
        menu.setIsCached(createDTO.getIsCached() != null ? createDTO.getIsCached() : 1);
        menu.setIsExternal(createDTO.getIsExternal() != null ? createDTO.getIsExternal() : 0);
        menu.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : 1);
        
        menuMapper.insert(menu);
    }

    /**
     * 更新菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuUpdateDTO updateDTO) {
        SysMenu menu = menuMapper.selectById(updateDTO.getId());
        if (menu == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 检查权限标识唯一性（排除自己）
        if (updateDTO.getPermissionKey() != null && !updateDTO.getPermissionKey().isEmpty()) {
            Long count = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getPermissionKey, updateDTO.getPermissionKey())
                    .ne(SysMenu::getId, updateDTO.getId())
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.PERMISSION_KEY_EXISTS);
            }
        }

        menu.setParentId(updateDTO.getParentId());
        menu.setMenuName(updateDTO.getMenuName());
        menu.setMenuType(updateDTO.getMenuType());
        menu.setPermissionKey(updateDTO.getPermissionKey());
        menu.setPath(updateDTO.getPath());
        menu.setComponent(updateDTO.getComponent());
        menu.setIcon(updateDTO.getIcon());
        menu.setSortOrder(updateDTO.getSortOrder() != null ? updateDTO.getSortOrder() : 0);
        menu.setIsVisible(updateDTO.getIsVisible() != null ? updateDTO.getIsVisible() : 1);
        menu.setIsCached(updateDTO.getIsCached() != null ? updateDTO.getIsCached() : 1);
        menu.setIsExternal(updateDTO.getIsExternal() != null ? updateDTO.getIsExternal() : 0);
        menu.setStatus(updateDTO.getStatus() != null ? updateDTO.getStatus() : 1);
        
        menuMapper.updateById(menu);
    }

    /**
     * 删除菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 检查是否有子菜单
        Long childCount = menuMapper.selectCount(
            new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException(ResultCode.MENU_HAS_CHILDREN);
        }

        menuMapper.deleteById(id);
    }

    /**
     * 构建菜单树
     */
    private List<MenuTreeVO> buildTree(List<MenuTreeVO> allMenus, Long parentId) {
        return allMenus.stream()
            .filter(menu -> menu.getParentId().equals(parentId))
            .peek(menu -> menu.setChildren(buildTree(allMenus, menu.getId())))
            .collect(Collectors.toList());
    }

    /**
     * 转换为TreeVO
     */
    private MenuTreeVO convertToTreeVO(SysMenu menu) {
        return MenuTreeVO.builder()
            .id(menu.getId())
            .parentId(menu.getParentId())
            .menuName(menu.getMenuName())
            .menuType(menu.getMenuType())
            .permissionKey(menu.getPermissionKey())
            .path(menu.getPath())
            .component(menu.getComponent())
            .icon(menu.getIcon())
            .sortOrder(menu.getSortOrder())
            .isVisible(menu.getIsVisible())
            .isCached(menu.getIsCached())
            .isExternal(menu.getIsExternal())
            .status(menu.getStatus())
            .createTime(menu.getCreateTime())
            .build();
    }
}
