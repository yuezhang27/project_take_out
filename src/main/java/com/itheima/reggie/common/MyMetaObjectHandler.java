package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        metaObject.setValue("createUser",BaseContext.getUserId());
        metaObject.setValue("updateUser",BaseContext.getUserId());
        //this.strictInsertFill(metaObject,"createUser", Long.class, BaseContext.getUserId());
        //this.strictUpdateFill(metaObject,"updateUser", Long.class, BaseContext.getUserId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        System.out.println(BaseContext.getUserId());
        //this.strictUpdateFill(metaObject,"updateUser", Long.class, BaseContext.getUserId());
        metaObject.setValue("updateUser",BaseContext.getUserId());
    }
}
