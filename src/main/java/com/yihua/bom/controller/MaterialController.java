package com.yihua.bom.controller;

import com.yihua.bom.dto.MaterialDTO;
import com.yihua.bom.entity.Material;
import com.yihua.bom.vo.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MaterialController {

    //validated注解比valid注解要更强一点
    //validated是Spring自己封装的 一个增强版本
    //valid是 JAVA EE的
    @PostMapping("")
    public Result<Material> addMaterial(@Validated @RequestBody MaterialDTO m){

          return Result.fail("测试数据");
    }
}
