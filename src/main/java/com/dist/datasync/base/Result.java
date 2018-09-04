package com.dist.datasync.base;

import java.io.Serializable;

/**
 * 通用业务信息传递机制类，定义了业务处理后的结果状态以及用来传递数据的对象
 * @author heshun
 * @version V1.0
 * @created 2014-08-13
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 业务状态：成功
     */
    public static final String SUCCESS = "success";
    /**
     * 业务状态：失败 因为业务逻辑错误导致操作失败，如邮箱已存在，年龄不满足条件等。
     */
    public static final String FAIL = "fail";
    /**
     * 业务状态：错误 因为一些不可预计的、系统级的错误导致的操作失败，如数据库断电，服务器内存溢出等。
     */
    public static final String ERROR = "error";

    /**
     * 业务状态，若业务状态是成功/失败/错误，则采用ReturnValue.SUCCESS、FAILED、ERROR。若非这3种状态，请自行定义。
     */
    private String status;
    /**
     * 业务信息，包含从业务异常中提取出的业务错误信息，也可以是自定义的业务信息，以及业务对象、业务对象集合等。
     */
    private T data;
    /**
     * 消息，可以用于存放提示内容
     */
    private String message;
    /**
     * 状态编码
     */
    private Integer code;

    /**
     * 公用无参的构造器
     */
    public Result() {
        super();
    }

    public Result(T data) {
        super();
        this.data = data;
    }

    public Result(String status) {
        super();
        this.status = status;
    }

    public Result(String status, T data) {
        super();
        this.status = status;
        this.data = data;
    }

    public Result(String status, T data, String message) {
        super();
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public Result(String status, T data, String message, Integer code) {
        super();
        this.status = status;
        this.data = data;
        this.message = message;
        this.code = code;
    }

    public static Result success(){
        return new Result(Result.SUCCESS);
    }

    public static Result success(Object data){
        return new Result(Result.SUCCESS, data);
    }

    public static Result success(Object data, String msg){
        return new Result(Result.SUCCESS, data, msg);
    }

    public static Result error(String msg){
        return new Result(Result.ERROR, null, msg);
    }

    public static Result error(Object data, String msg){
        return new Result(Result.ERROR, data, msg);
    }

    public static Result fail(String msg){
        return new Result(Result.FAIL, null, msg);
    }

    public static Result fail(Object data, String msg){
        return new Result(Result.FAIL, data, msg);
    }

    public static Result of(BooleanWithMessage data){
        String status = data.getState()? Result.SUCCESS: Result.FAIL;
        return new Result(status, null, data.getMessage());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}

