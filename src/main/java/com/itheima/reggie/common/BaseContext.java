package com.itheima.reggie.common;

public class BaseContext{
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();
    public static void setUserId(Long id){
        threadLocal.set(id);
    }
    public static Long getUserId(){
        return threadLocal.get();
    }
}
