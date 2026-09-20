package com.furinafans.stusys.common;




public class Result<T> {
    private static final Integer SUCCESS = 200;
    private static final Integer FAIL = 500;

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
        return new Result<>(FAIL,FAIL_MSG);
    }

    //失败 传自定义失败消息
    public static <T> Result<T> fail(String msg){
        return new Result<>(FAIL,msg);
    }
}
