package com.furinafans.stusys.handler;

import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.common.constant.ErrorCode;
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

    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccessException(DataAccessException e){
        log.error("数据库异常:code = {}, msg = {}", ErrorCode.DATA_ERROR, e.getMessage(), e);
        return Result.fail(ErrorCode.DATA_ERROR, "数据库异常");
    }

    @ExceptionHandler(Exception.class) 
    public Result<Void> handleOtherException(Exception e){
        log.error("未知的异常", e);
        return Result.fail();
    }
}
