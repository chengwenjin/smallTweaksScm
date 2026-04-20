package com.baserbac.common.exception;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return R.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return R.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理其他异常（排除 NoResourceFoundException）
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) throws Exception {
        // 不处理 NoResourceFoundException，让 Spring Boot 默认处理静态资源
        if (e instanceof NoResourceFoundException) {
            throw e; // 重新抛出，让 Spring Boot 默认处理
        }
        log.error("系统异常: ", e);
        return R.error(ResultCode.INTERNAL_ERROR);
    }
}
