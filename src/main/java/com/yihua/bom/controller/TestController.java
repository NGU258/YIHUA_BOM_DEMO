package com.yihua.bom.controller;

import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/good")
    public String good(){
        return "Good evening~";
    }

    @PostMapping("/evening")
    public A evening(){
        return new A("",-66);
    }

    @PostMapping("/testValidation")
    public A testValidation(@Valid @RequestBody A a){
        return a;
    }
}

class A{
    @NotNull(message="注意姓名不能为空哦")
    private String name;

    @Min(value = 1,message="年龄必须大于0喵~")
    private Integer age;

    public A(){

    }
    public A(String name,Integer age){
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
