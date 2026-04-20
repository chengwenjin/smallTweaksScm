package com.baserbac.service;

import com.baserbac.entity.SysLoginLog;
import com.baserbac.mapper.LoginLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 登录日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogMapper loginLogMapper;

    /**
     * 分页查询登录日志
     */
    public Page<SysLoginLog> pageLoginLogs(String username, Integer loginStatus, 
                                            String startTime, String endTime, int pageNum, int pageSize) {
        Page<SysLoginLog> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null, SysLoginLog::getUsername, username)
               .eq(loginStatus != null, SysLoginLog::getLoginStatus, loginStatus);
        
        // 时间范围查询
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(SysLoginLog::getLoginTime, LocalDateTime.parse(startTime, formatter));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(SysLoginLog::getLoginTime, LocalDateTime.parse(endTime, formatter));
        }
        
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        
        return loginLogMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询登录日志
     */
    public SysLoginLog getLoginLogById(Long id) {
        return loginLogMapper.selectById(id);
    }

    /**
     * 删除登录日志
     */
    public void deleteLoginLog(Long id) {
        loginLogMapper.deleteById(id);
    }

    /**
     * 批量删除登录日志
     */
    public void batchDeleteLoginLogs(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            loginLogMapper.deleteById(Long.parseLong(id.trim()));
        }
    }

    /**
     * 清空所有登录日志
     */
    public void clearAllLoginLogs() {
        loginLogMapper.delete(null);
    }
}
