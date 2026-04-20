package com.baserbac.aspect;

import com.baserbac.common.util.IpUtil;
import com.baserbac.common.util.SecurityUtil;
import com.baserbac.entity.SysOperationLog;
import com.baserbac.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志AOP切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(com.baserbac.annotation.OperationLog)")
    public Object recordOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperationLog logEntity = new SysOperationLog();
        
        log.info("========== 操作日志切面开始执行 ==========");
        log.info("切点方法: {}", joinPoint.getSignature().getName());
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.baserbac.annotation.OperationLog operationLog = method.getAnnotation(com.baserbac.annotation.OperationLog.class);
        
        if (operationLog == null) {
            log.warn("未找到@OperationLog注解，跳过日志记录");
            return joinPoint.proceed();
        }
        
        // 设置日志基础信息
        logEntity.setLogType(2); // 2-操作日志
        logEntity.setModule(operationLog.module());
        logEntity.setDescription(operationLog.value());
        logEntity.setOperateTime(LocalDateTime.now());
        
        log.info("日志信息 - 模块: {}, 描述: {}", operationLog.module(), operationLog.value());
        
        // 获取用户信息
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            String username = SecurityUtil.getCurrentUsername();
            logEntity.setOperatorId(userId);
            logEntity.setOperatorName(username);
            log.info("当前用户 - ID: {}, 用户名: {}", userId, username);
        } catch (Exception e) {
            log.warn("获取当前用户信息失败", e);
        }
        
        // 设置请求信息
        if (request != null) {
            logEntity.setRequestMethod(request.getMethod());
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setIp(IpUtil.getIpAddress(request));
            logEntity.setUserAgent(request.getHeader("User-Agent"));
            log.info("请求信息 - 方法: {}, URL: {}", request.getMethod(), request.getRequestURI());
        }
        
        try {
            // 执行业务方法
            log.info("开始执行业务方法...");
            Object result = joinPoint.proceed();
            log.info("业务方法执行成功");
            
            // 记录成功日志
            logEntity.setStatus(1);
            logEntity.setDuration((int) (System.currentTimeMillis() - startTime));
            
            log.info("准备插入操作日志，日志对象: {}", logEntity);
            
            // 异步插入日志，不影响主业务
            try {
                int rows = operationLogMapper.insert(logEntity);
                log.info("操作日志插入成功，影响行数: {}", rows);
            } catch (Exception e) {
                log.error("插入操作日志失败", e);
            }
            
            return result;
        } catch (Throwable e) {
            log.error("业务方法执行异常", e);
            
            // 记录失败日志
            logEntity.setStatus(0);
            logEntity.setErrorMsg(e.getMessage());
            logEntity.setDuration((int) (System.currentTimeMillis() - startTime));
            
            log.info("准备插入失败操作日志，日志对象: {}", logEntity);
            
            // 异步插入日志，不影响主业务
            try {
                int rows = operationLogMapper.insert(logEntity);
                log.info("失败操作日志插入成功，影响行数: {}", rows);
            } catch (Exception ex) {
                log.error("插入操作日志失败", ex);
            }
            
            throw e;
        }
    }
}
