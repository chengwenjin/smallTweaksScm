package com.baserbac.service;

import com.baserbac.common.constant.CommonConstant;
import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.common.util.PasswordUtil;
import com.baserbac.dto.AssignRoleDTO;
import com.baserbac.dto.ResetPasswordDTO;
import com.baserbac.dto.UserCreateDTO;
import com.baserbac.dto.UserQueryDTO;
import com.baserbac.dto.UserStatusDTO;
import com.baserbac.dto.UserUpdateDTO;
import com.baserbac.entity.SysUser;
import com.baserbac.entity.SysUserRole;
import com.baserbac.mapper.UserMapper;
import com.baserbac.mapper.UserRoleMapper;
import com.baserbac.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 分页查询用户列表
     */
    public PageResult<UserVO> pageUsers(UserQueryDTO queryDTO) {
        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getUsername() != null, SysUser::getUsername, queryDTO.getUsername())
               .like(queryDTO.getRealName() != null, SysUser::getRealName, queryDTO.getRealName())
               .eq(queryDTO.getPhone() != null, SysUser::getPhone, queryDTO.getPhone())
               .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
               .orderByDesc(SysUser::getCreateTime);
        
        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        
        List<UserVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    /**
     * 根据ID查询用户
     */
    public UserVO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(user);
    }

    /**
     * 创建用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateDTO createDTO) {
        // 检查用户名是否存在
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, createDTO.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(createDTO.getUsername());
        user.setPassword(PasswordUtil.encode(createDTO.getPassword()));
        user.setRealName(createDTO.getRealName());
        user.setPhone(createDTO.getPhone());
        user.setEmail(createDTO.getEmail());
        user.setAvatar(createDTO.getAvatar());
        user.setStatus(createDTO.getStatus());
        user.setRemark(createDTO.getRemark());
        
        userMapper.insert(user);

        // 分配角色
        if (createDTO.getRoleIds() != null && !createDTO.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), createDTO.getRoleIds());
        }
    }

    /**
     * 更新用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDTO updateDTO) {
        SysUser user = userMapper.selectById(updateDTO.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 更新用户信息
        user.setRealName(updateDTO.getRealName());
        user.setPhone(updateDTO.getPhone());
        user.setEmail(updateDTO.getEmail());
        user.setAvatar(updateDTO.getAvatar());
        if (updateDTO.getStatus() != null) {
            user.setStatus(updateDTO.getStatus());
        }
        user.setRemark(updateDTO.getRemark());
        
        userMapper.updateById(user);

        // 更新角色
        if (updateDTO.getRoleIds() != null) {
            // 先删除旧的角色关联
            userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, updateDTO.getId())
            );
            // 再添加新的角色关联
            if (!updateDTO.getRoleIds().isEmpty()) {
                assignRoles(updateDTO.getId(), updateDTO.getRoleIds());
            }
        }
    }

    /**
     * 删除用户（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 不允许删除超级管理员
        if (CommonConstant.SUPER_ADMIN_USER_ID.equals(id)) {
            throw new BusinessException(ResultCode.SYSTEM_DATA_PROTECTED);
        }

        // 软删除用户
        userMapper.deleteById(id);
        
        // 删除角色关联
        userRoleMapper.delete(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id)
        );
    }

    /**
     * 更新用户状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, UserStatusDTO statusDTO) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 不允许操作超级管理员
        if (CommonConstant.SUPER_ADMIN_USER_ID.equals(id)) {
            throw new BusinessException(ResultCode.SYSTEM_DATA_PROTECTED);
        }

        user.setStatus(statusDTO.getStatus());
        userMapper.updateById(user);
    }

    /**
     * 重置密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, ResetPasswordDTO passwordDTO) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 不允许重置超级管理员密码
        if (CommonConstant.SUPER_ADMIN_USER_ID.equals(id)) {
            throw new BusinessException(ResultCode.SYSTEM_DATA_PROTECTED);
        }

        user.setPassword(PasswordUtil.encode(passwordDTO.getNewPassword()));
        user.setMustChangePwd(1);
        userMapper.updateById(user);
    }

    /**
     * 分配角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long id, AssignRoleDTO roleDTO) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 不允许操作超级管理员的角色
        if (CommonConstant.SUPER_ADMIN_USER_ID.equals(id)) {
            throw new BusinessException(ResultCode.SYSTEM_DATA_PROTECTED);
        }

        // 先删除旧的角色关联
        userRoleMapper.delete(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id)
        );
        
        // 再添加新的角色关联
        if (roleDTO.getRoleIds() != null && !roleDTO.getRoleIds().isEmpty()) {
            assignRoles(id, roleDTO.getRoleIds());
        }
    }

    /**
     * 分配角色（内部方法）
     */
    private void assignRoles(Long userId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    /**
     * 转换为VO
     */
    private UserVO convertToVO(SysUser user) {
        // 查询用户的角色列表
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getId())
        );
        
        List<Long> roleIds = userRoles.stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        
        return UserVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .phone(user.getPhone())
            .email(user.getEmail())
            .avatar(user.getAvatar())
            .status(user.getStatus())
            .roleIds(roleIds)
            .lastLoginTime(user.getLastLoginTime())
            .lastLoginIp(user.getLastLoginIp())
            .remark(user.getRemark())
            .createTime(user.getCreateTime())
            .build();
    }
}
