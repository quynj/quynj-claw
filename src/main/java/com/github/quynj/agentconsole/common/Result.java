package com.github.quynj.agentconsole.common;

public class Result<T> {
    public int code;
    public String message;
    public T data;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.message = "ok";
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = 1;
        result.message = message;
        return result;
    }
}
