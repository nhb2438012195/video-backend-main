package com.nhb.handler;

import com.nhb.exception.RegisterFailedException;
import com.nhb.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

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



}
