package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice(annotations = {RestController.class, Controller.class})
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> sqlExceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message=ex.getMessage();
        log.info(message);
        if(message.contains("Duplicate entry")){
            String[] split=message.split(" ");
            String msg=split[2]+"already exist";
            return Result.error(msg);
        }
        return Result.error("unknown error");
    }
    @ExceptionHandler(CustomException.class)
    public Result<String> customExceptionHandler(CustomException customException){
        return Result.error(customException.getMessage());
    }
}
