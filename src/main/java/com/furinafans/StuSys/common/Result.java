package com.furinafans.stusys.common;

import com.furinafans.stusys.common.enums.ErrorCode;

public class Result<T> {
    private static final Integer SUCCESS = ErrorCode.SUCCESS;
    public static final Integer PARAM_ERROR = ErrorCode.PARAM_ERROR;
    public static final Integer NOT_FOUND = ErrorCode.NOT_FOUND;
    public static final Integer FAIL = ErrorCode.SERVER_ERROR;

    private static final String SUCCESS_MSG = "success";
    private static final String FAIL_MSG = "fail";

    private Integer code;
    private String msg;
    private T data;


    private Result(Integer code, String msg, T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private Result(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }


    
    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    //成功 传默认成功消息 不带返回数据
    public static <T> Result<T> success(){
        return new Result<>(SUCCESS, SUCCESS_MSG);
    }

    //成功 传默认成功消息 带返回数据
    public static <T> Result<T> success(T data){
        return new Result<>(SUCCESS, SUCCESS_MSG, data);
    }
    
    //失败 传默认失败消息
    public static <T> Result<T> fail(){
        return fail(FAIL,FAIL_MSG);
    }

    //失败 传自定义失败消息
    public static <T> Result<T> fail(String msg){
        return fail(FAIL,msg);
    }

    public static <T> Result <T> fail (Integer code, String msg){
        return new Result<>(code, msg);
    }
}
