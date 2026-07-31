package com.yihua.bom.controller;

import com.yihua.bom.dto.MaterialDTO;
import com.yihua.bom.entity.Material;
import com.yihua.bom.service.IMaterialService;
import com.yihua.bom.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/material")
public class MaterialController {

    private final IMaterialService iMaterailService;

    //validated注解比valid注解要更强一点
    //validated是Spring自己封装的 一个增强版本
    //valid是 JAVA EE的
    @PostMapping("/create")
    public Result<Material> addMaterial(@Validated @RequestBody MaterialDTO m){

          Material material = iMaterailService.createMaterial(m);

          return Result.success("插入成功",material);
    }

}
