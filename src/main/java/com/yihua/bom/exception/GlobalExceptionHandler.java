package com.yihua.bom.exception;

import com.yihua.bom.vo.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

//Rest代表@RestController，方法的返回值会自动转换成JSON
//@ControllerAdvice会捕获所有的异常 凡是有异常都会来到这里
//而@RestControllerAdvice就是上面这两者的结合
//使用@ExceptionHandler注解来指定要处理哪个异常
@RestControllerAdvice
public class GlobalExceptionHandler {

    //    返回值里面的Void是一个特殊类 表示没有值
    //    处理校验异常 MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> ValidationExceptionHandler(MethodArgumentNotValidException e){

        //思路： 将相关的异常信息以分号的形式拼接 然后再返回给用户看 例如 异常1;异常2;……
        String errorMessage = e.getBindingResult() //获取存储异常信息的结果对象
                .getFieldErrors() //拿出该对象中的所有错误字段
                .stream() //转成流(流水线 一个螺丝一个螺丝的送)
                .map(FieldError::getDefaultMessage) //获取所有错误字段对应的错误信息  这里就对应着校验注解对应的value值了
                .collect(Collectors.joining(";"));//收集起来 然后用分号分隔

        return Result.fail(errorMessage);
    }

    //捕获抛出的自定义异常
    @ExceptionHandler(fairyCatException.class)
    public Result<Void> fairyCatExceptionHandler(fairyCatException e){
        return Result.fail(e.getCode(),e.getMessage());
    }

    //捕获所有未被处理的异常
    @ExceptionHandler(Exception.class)
    public Result<Void> otherExceptionhandler(Exception e){
        return Result.fail("500","服务器内部错误(请看后端控制台输出):"+e.getMessage());
    }

}
