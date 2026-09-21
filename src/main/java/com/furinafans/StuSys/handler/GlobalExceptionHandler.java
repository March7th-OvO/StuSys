package com.furinafans.stusys.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.exception.BizException;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e){
        log.warn("业务异常:code = {}, msg = {}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class) 
    public Result<Void> handleOtherException(Exception e){
        log.error("未知的异常", e);
        return Result.fail();
    }
}
