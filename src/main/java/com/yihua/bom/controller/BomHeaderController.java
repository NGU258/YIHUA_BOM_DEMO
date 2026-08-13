package com.yihua.bom.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yihua.bom.dto.BomHeaderDTO;
import com.yihua.bom.entity.BomHeader;
import com.yihua.bom.service.IBomHeaderService;
import com.yihua.bom.service.impl.BomHeaderServiceImpl;
import com.yihua.bom.vo.BomHeaderVo;
import com.yihua.bom.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/boms")
public class BomHeaderController {

    private final IBomHeaderService iBomHeaderService;

    public BomHeaderController(IBomHeaderService b){
        iBomHeaderService = b;
    }

    @PostMapping
    public Result<BomHeader> createBomHeader(@RequestBody BomHeaderDTO b){
            BomHeader bomHeader = iBomHeaderService.createBomHeader(b);
            return Result.success("插入成功",bomHeader);
    }

    @GetMapping
    public Result<IPage<BomHeader>> listBomHeader(
                                                  //这里写0或1的效果是一样的 最后生成的SQL语句都是limit 10;
                                                  @RequestParam(defaultValue = "0")  Long curPage,
                                                  @RequestParam(defaultValue = "10") Long curPageNum,
                                                  @RequestParam String keyword){
            IPage<BomHeader> result  = iBomHeaderService.listBomHeader(curPage,curPageNum,keyword);
            return Result.success("分页成功",result);
    }

    //这里除了需要查询Bom主表中的记录之外
    //还需要查询这个记录对应的BOM子表中的记录
    //所以这里的设计思路就是使用Map来实现 key用来存储对应的字段 value就是对应的表中的明细
    @GetMapping("/{bomId}")
    public Result<Map<String, Object>> getBomHeaderAnItemsById(@PathVariable Long bomId){
            Map<String,Object> result = iBomHeaderService.getBomHeaderAndItemsByBomId(bomId);
            return Result.success("查询成功",result);
    }

    @PutMapping("/{bomId}")
    public Result<BomHeader> updateBomHeader(@PathVariable Long bomId,@RequestBody BomHeaderDTO b){
            BomHeader  bomHeader = iBomHeaderService.updateBomHeaderByBomId(bomId,b);
            return Result.success("更新成功",bomHeader);
    }

    //通过bomId来删除Bom主表中的记录以及Bom子表中的相关联的明细
    @DeleteMapping("/{bomId}")
    public Result<Map<String,Object>> deleteBomHeaderAndItemByBomId(@PathVariable Long bomId){

        Map<String,Object> bomHeader = iBomHeaderService.deleteBomHeaderAndItemByBomId(bomId);
        return Result.success("删除成功",bomHeader);

    }

    @PutMapping("/{bomId}/active")
    public Result<BomHeaderVo> activeBomStatus(@PathVariable Long bomId){
            BomHeaderVo bhVo  = iBomHeaderService.activeBomStatus(bomId);
            return Result.success("启动成功",bhVo);
    }

    @PutMapping("/{bomId}/disable")
    public Result<BomHeaderVo> disableBomStatus(@PathVariable Long bomId){
         BomHeaderVo bomHeaderVo = iBomHeaderService.disableBomStatus(bomId);
         return Result.success("停用成功",bomHeaderVo);
    }

    @PostMapping("/{bomId}/copy")
    public Result<Map<String,Object>> copyBomHeaderAndBomItemByBomId(@PathVariable Long bomId){
            Map<String,Object> copyBomMap = iBomHeaderService.copyBomHeaderAndBomItemByBomId(bomId);
            return Result.success("复制成功",copyBomMap);
    }

}
