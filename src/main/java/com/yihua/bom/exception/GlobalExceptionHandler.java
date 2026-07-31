package com.yihua.bom.exception;

import com.yihua.bom.vo.Result;
import org.springframework.dao.DuplicateKeyException;
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

    //捕获唯一索引抛出的异常
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> duplicateKeyExceptionHandler(DuplicateKeyException e){


        //保存错误信息的异常父类可以通过getCasue方法获取到
        //然后再调用getMessage方法来获取里面具体的错误信息
        //下面的errorMessage打印的就是： Duplicate entry 'BOM-A-001' for key 'material.uk_material_code'
        //这里的material.uk_material_code 就是对应的字段的索引名
        //我只需要用正则表达式来获取.后面的名称
        //然后再使用substring方法来获取uk_后面的字段名 就可以达到预料的效果了
        String errorMessage = e.getCause().getMessage();

        System.out.println(errorMessage);

//        // 拿到最底层的异常信息
//        String message = e.getCause().getMessage();
//
//        // 用正则提取 "for key 'xxx'" 中的 xxx
//        String keyName = "未知";
//        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("for key '(.+?)'").matcher(message);
//        if (matcher.find()) {
//            keyName = matcher.group(1);  // 例如 "material.uk_material_code"
//
//            // 如果有表名前缀（如 material.），去掉它
//            if (keyName.contains(".")) {
//                keyName = keyName.substring(keyName.lastIndexOf(".") + 1);
//            }
//        }
        return Result.fail("有一列的值在数据库中已经存在了");
    }

    //捕获所有未被处理的异常
    @ExceptionHandler(Exception.class)
    public Result<Void> otherExceptionhandler(Exception e){
        e.printStackTrace();
        return Result.fail("500","服务器内部错误(请看后端控制台输出):"+e.getMessage());
    }

}
