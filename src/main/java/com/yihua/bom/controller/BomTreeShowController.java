package com.yihua.bom.controller;

import com.yihua.bom.service.IMaterialService;
import com.yihua.bom.vo.BomTreeStructVo;
import com.yihua.bom.vo.MateiralVo;
import com.yihua.bom.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    //统计生产指定物料(成品或半成品)需要多少原材料
    @GetMapping("/{materialId}/summary")
    public Result<List<MateiralVo>> summaryTotalQtyByMaterialId(@PathVariable Long materialId){
        List<MateiralVo> materialList = iMaterialService.summaryToTalQtyByMaterialId(materialId);
        return Result.success("已成功汇总生产该物料所需要的标准用量",materialList);

    }
}
