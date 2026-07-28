package com.yihua.bom.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//Rest代表@RestController 方法的返回值会自动转换成JSON
@RestControllerAdvice
public class GlobalExceptionHandler {

//    处理校验异常 MethodArgumentNotValidException
    @ExceptionHandler(value= MethodArgumentNotValidException.class)
    public void ValidationExceptionHandler(MethodArgumentNotValidException e){

    }
}
