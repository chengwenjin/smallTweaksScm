package com.baserbac.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.entity.SysApiPermission;
import com.baserbac.mapper.ApiPermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * API权限服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiPermissionService {

    private final ApiPermissionMapper apiPermissionMapper;

    /**
     * 分页查询API权限
     */
    public Page<SysApiPermission> pageApiPermissions(String apiName, String permissionKey, Integer status, int pageNum, int pageSize) {
        Page<SysApiPermission> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<SysApiPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(apiName != null, SysApiPermission::getApiName, apiName)
               .like(permissionKey != null, SysApiPermission::getPermissionKey, permissionKey)
               .eq(status != null, SysApiPermission::getStatus, status)
               .orderByDesc(SysApiPermission::getCreateTime);
        
        return apiPermissionMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询API权限
     */
    public SysApiPermission getApiPermissionById(Long id) {
        SysApiPermission permission = apiPermissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return permission;
    }

    /**
     * 创建API权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void createApiPermission(SysApiPermission permission) {
        // 检查权限标识唯一性
        Long count = apiPermissionMapper.selectCount(
            new LambdaQueryWrapper<SysApiPermission>()
                .eq(SysApiPermission::getPermissionKey, permission.getPermissionKey())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.PERMISSION_KEY_EXISTS);
        }

        apiPermissionMapper.insert(permission);
    }

    /**
     * 更新API权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApiPermission(SysApiPermission permission) {
        SysApiPermission existPermission = apiPermissionMapper.selectById(permission.getId());
        if (existPermission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 检查权限标识唯一性（排除自己）
        Long count = apiPermissionMapper.selectCount(
            new LambdaQueryWrapper<SysApiPermission>()
                .eq(SysApiPermission::getPermissionKey, permission.getPermissionKey())
                .ne(SysApiPermission::getId, permission.getId())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.PERMISSION_KEY_EXISTS);
        }

        apiPermissionMapper.updateById(permission);
    }

    /**
     * 删除API权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteApiPermission(Long id) {
        SysApiPermission permission = apiPermissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        apiPermissionMapper.deleteById(id);
    }

    /**
     * 获取所有启用的API权限
     */
    public List<SysApiPermission> getAllEnabledPermissions() {
        return apiPermissionMapper.selectList(
            new LambdaQueryWrapper<SysApiPermission>()
                .eq(SysApiPermission::getStatus, 1)
                .orderByDesc(SysApiPermission::getCreateTime)
        );
    }
}
