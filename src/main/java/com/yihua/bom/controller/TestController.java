package com.yihua.bom.controller;

import com.yihua.bom.entity.Material;
import com.yihua.bom.exception.fairyCatException;
import com.yihua.bom.vo.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/good")
    public String good(){
        return "Good evening~";
    }

    @PostMapping("/testValidation")
    public Result<Material> testValidation(@Valid @RequestBody Material a){
        return Result.success("成功访问喵~",a);
    }

    @PostMapping("/testException")
    public Result<Material> testExceptionHandler(@RequestBody Material m){
        throw new fairyCatException("JSON字段有问题喵~");

    }
}


