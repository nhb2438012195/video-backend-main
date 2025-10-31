package com.nhb.handler;

import com.nhb.exception.BusinessException;
import com.nhb.exception.RegisterFailedException;
import com.nhb.result.Result;
import io.minio.messages.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(RegisterFailedException.class)
    public Result handleRegisterFailedException(RegisterFailedException e) {
        log.error("注册失败：{}", e.getMessage());
        return Result.error(e.getMessage());
    }
    @ExceptionHandler(BusinessException.class)
    public Result<Error> handleBusiness(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }
    //非业务异常不要捕获，统一处理
    @ExceptionHandler(Exception.class)
    public Result<String> handleUnexpectedException(Exception e) {
        // 获取当前请求信息（用于日志）
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = "unknown";
        String method = "unknown";
        String ip = "unknown";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            uri = request.getRequestURI();
            method = request.getMethod();
            ip = getClientIpAddress(request);
        }
        // 生成可读时间（可选）
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .format(LocalDateTime.now());

        // 🔥 关键：记录完整异常（含堆栈）+ 上下文信息
        log.error(
                "\n=== 系统异常（兜底捕获） ===\n" +
                        "时间: {}\n" +
                        "请求: {} {}\n" +
                        "来源IP: {}\n" +
                        "异常类型: {}\n" +
                        "异常消息: {}\n" +
                        "堆栈跟踪:",
                timestamp, method, uri, ip, e.getClass().getSimpleName(), e.getMessage(), e
        );

        return Result.error("服务暂时不可用，请稍后再试"+e.getMessage());
    }

    // 工具方法：获取真实客户端 IP（考虑代理）
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }


}
