package com.itheima.reggie.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;
    private Map map=new HashMap();
    public static<T> Result<T> success(T object){
        Result<T> result=new Result<T>();
        result.setData(object);
        result.setCode(1);
        return result;
    }
    public static <T> Result<T> error(String message){
        Result<T> result=new Result<T>();
        result.setMsg(message);
        result.setCode(0);
        result.setData(null);
        return result;
    }

    public Result<T> add(String key, Object value) {
        this.map.put(key,value);
        return this;
    }
}
