package com.baserbac.service;

import com.baserbac.entity.SysOperationLog;
import com.baserbac.mapper.OperationLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 分页查询操作日志
     */
    public Page<SysOperationLog> pageOperationLogs(String operatorName, String module, Integer logType, 
                                                     String startTime, String endTime, int pageNum, int pageSize) {
        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(operatorName != null, SysOperationLog::getOperatorName, operatorName)
               .like(module != null, SysOperationLog::getModule, module)
               .eq(logType != null, SysOperationLog::getLogType, logType);
        
        // 时间范围查询
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(SysOperationLog::getOperateTime, LocalDateTime.parse(startTime, formatter));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(SysOperationLog::getOperateTime, LocalDateTime.parse(endTime, formatter));
        }
        
        wrapper.orderByDesc(SysOperationLog::getOperateTime);
        
        return operationLogMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询操作日志
     */
    public SysOperationLog getOperationLogById(Long id) {
        return operationLogMapper.selectById(id);
    }

    /**
     * 删除操作日志
     */
    public void deleteOperationLog(Long id) {
        operationLogMapper.deleteById(id);
    }

    /**
     * 批量删除操作日志
     */
    public void batchDeleteOperationLogs(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            operationLogMapper.deleteById(Long.parseLong(id.trim()));
        }
    }

    /**
     * 清空所有操作日志
     */
    public void clearAllOperationLogs() {
        operationLogMapper.delete(null);
    }
}
