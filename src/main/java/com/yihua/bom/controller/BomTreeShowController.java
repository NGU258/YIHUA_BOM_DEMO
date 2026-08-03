package com.yihua.bom.controller;

import com.yihua.bom.service.IMaterialService;
import com.yihua.bom.vo.BomTreeStructVo;
import com.yihua.bom.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boms/material")
public class BomTreeShowController {

    private final IMaterialService iMaterialService;

    public BomTreeShowController(IMaterialService im){
        iMaterialService = im ;
    }

    @GetMapping("/{materialId}/tree")
    public Result<BomTreeStructVo> BomTreeStructByMaterialId(@PathVariable Long materialId){
            BomTreeStructVo bomTreeStructVo = iMaterialService.BomTreeStructByMaterialId(materialId);
            return Result.success("BOM树查询成功",bomTreeStructVo);
    }
}
