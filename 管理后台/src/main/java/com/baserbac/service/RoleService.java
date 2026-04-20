package com.baserbac.service;

import com.baserbac.common.constant.CommonConstant;
import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.dto.AssignPermDTO;
import com.baserbac.dto.RoleCreateDTO;
import com.baserbac.dto.RoleQueryDTO;
import com.baserbac.dto.RoleStatusDTO;
import com.baserbac.dto.RoleUpdateDTO;
import com.baserbac.entity.SysRole;
import com.baserbac.entity.SysRoleApi;
import com.baserbac.entity.SysRoleMenu;
import com.baserbac.mapper.RoleApiMapper;
import com.baserbac.mapper.RoleMapper;
import com.baserbac.mapper.RoleMenuMapper;
import com.baserbac.vo.RoleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RoleApiMapper roleApiMapper;

    /**
     * 分页查询角色列表
     */
    public PageResult<RoleVO> pageRoles(RoleQueryDTO queryDTO) {
        Page<SysRole> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getRoleName() != null, SysRole::getRoleName, queryDTO.getRoleName())
               .like(queryDTO.getRoleKey() != null, SysRole::getRoleKey, queryDTO.getRoleKey())
               .eq(queryDTO.getStatus() != null, SysRole::getStatus, queryDTO.getStatus())
               .orderByAsc(SysRole::getSortOrder);
        
        Page<SysRole> result = roleMapper.selectPage(page, wrapper);
        
        List<RoleVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), voList, (long) result.getCurrent(), (long) result.getSize());
    }

    /**
     * 查询所有角色
     */
    public List<RoleVO> listAllRoles() {
        List<SysRole> roles = roleMapper.selectList(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSortOrder)
        );
        return roles.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 根据ID查询角色
     */
    public RoleVO getRoleById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(role);
    }

    /**
     * 创建角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleCreateDTO createDTO) {
        // 检查角色标识是否存在
        Long count = roleMapper.selectCount(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, createDTO.getRoleKey())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.ROLE_KEY_EXISTS);
        }

        SysRole role = new SysRole();
        role.setRoleName(createDTO.getRoleName());
        role.setRoleKey(createDTO.getRoleKey());
        role.setSortOrder(createDTO.getSortOrder() != null ? createDTO.getSortOrder() : 0);
        role.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : 1);
        role.setRemark(createDTO.getRemark());
        
        roleMapper.insert(role);
    }

    /**
     * 更新角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateDTO updateDTO) {
        SysRole role = roleMapper.selectById(updateDTO.getId());
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 不允许修改超级管理员角色
        if (CommonConstant.SUPER_ADMIN_ROLE_ID.equals(updateDTO.getId())) {
            throw new BusinessException(ResultCode.SYSTEM_DATA_PROTECTED);
        }

        role.setRoleName(updateDTO.getRoleName());
        role.setRoleKey(updateDTO.getRoleKey());
        role.setSortOrder(updateDTO.getSortOrder() != null ? updateDTO.getSortOrder() : 0);
        role.setStatus(updateDTO.getStatus() != null ? updateDTO.getStatus() : 1);
        role.setRemark(updateDTO.getRemark());
        
        roleMapper.updateById(role);
    }

    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 不允许删除超级管理员角色
        if (CommonConstant.SUPER_ADMIN_ROLE_ID.equals(id)) {
            throw new BusinessException(ResultCode.SYSTEM_DATA_PROTECTED);
        }

        roleMapper.deleteById(id);
        
        // 删除角色菜单关联
        roleMenuMapper.delete(
            new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, id)
        );
        
        // 删除角色API关联
        roleApiMapper.delete(
            new LambdaQueryWrapper<SysRoleApi>()
                .eq(SysRoleApi::getRoleId, id)
        );
    }

    /**
     * 更新角色状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleStatus(Long id, RoleStatusDTO statusDTO) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 不允许修改超级管理员角色状态
        if (CommonConstant.SUPER_ADMIN_ROLE_ID.equals(id)) {
            throw new BusinessException(ResultCode.SYSTEM_DATA_PROTECTED);
        }

        role.setStatus(statusDTO.getStatus());
        roleMapper.updateById(role);
    }

    /**
     * 分配权限（菜单+API）
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long id, AssignPermDTO permDTO) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 不允许修改超级管理员角色的权限
        if (CommonConstant.SUPER_ADMIN_ROLE_ID.equals(id)) {
            throw new BusinessException(ResultCode.SYSTEM_DATA_PROTECTED);
        }

        // 删除旧的菜单关联
        roleMenuMapper.delete(
            new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, id)
        );
        
        // 添加新的菜单关联
        if (permDTO.getMenuIds() != null && !permDTO.getMenuIds().isEmpty()) {
            for (Long menuId : permDTO.getMenuIds()) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(id);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }

        // 删除旧的API关联
        roleApiMapper.delete(
            new LambdaQueryWrapper<SysRoleApi>()
                .eq(SysRoleApi::getRoleId, id)
        );
        
        // 添加新的API关联
        if (permDTO.getApiIds() != null && !permDTO.getApiIds().isEmpty()) {
            for (Long apiId : permDTO.getApiIds()) {
                SysRoleApi roleApi = new SysRoleApi();
                roleApi.setRoleId(id);
                roleApi.setApiId(apiId);
                roleApiMapper.insert(roleApi);
            }
        }

        log.info("角色 {} 的权限已更新", id);
    }

    /**
     * 转换为VO
     */
    private RoleVO convertToVO(SysRole role) {
        // 查询角色的菜单ID列表
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
            new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, role.getId())
        );
        List<Long> menuIds = roleMenus.stream()
            .map(SysRoleMenu::getMenuId)
            .collect(Collectors.toList());

        // 查询角色的API权限ID列表
        List<SysRoleApi> roleApis = roleApiMapper.selectList(
            new LambdaQueryWrapper<SysRoleApi>()
                .eq(SysRoleApi::getRoleId, role.getId())
        );
        List<Long> apiIds = roleApis.stream()
            .map(SysRoleApi::getApiId)
            .collect(Collectors.toList());

        return RoleVO.builder()
            .id(role.getId())
            .roleName(role.getRoleName())
            .roleKey(role.getRoleKey())
            .sortOrder(role.getSortOrder())
            .status(role.getStatus())
            .remark(role.getRemark())
            .createTime(role.getCreateTime())
            .menuIds(menuIds)
            .apiIds(apiIds)
            .build();
    }
}
