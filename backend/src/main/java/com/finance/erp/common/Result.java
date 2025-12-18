package com.finance.erp.common;

import lombok.Data;

/**
 * 统一 API 响应结果封装
 * 所有 Controller 的返回类型都应该是 Result<T>
 */
@Data
public class Result<T> {
    private Integer code; // 200: 成功, 500: 错误
    private String msg;   // 提示信息
    private T data;       // 返回数据

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }
    
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}