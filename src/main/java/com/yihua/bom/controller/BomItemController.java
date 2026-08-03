package com.yihua.bom.controller;

import com.yihua.bom.dto.BomItemDTO;
import com.yihua.bom.entity.BomItem;
import com.yihua.bom.service.IBomItemService;
import com.yihua.bom.vo.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boms/{bomId}/items")
public class BomItemController {

    private final IBomItemService iBomItemService;

    public BomItemController(IBomItemService ibis){
        this.iBomItemService = ibis;
    }

    @PostMapping
    public Result<BomItem> createBomItem(@PathVariable Long bomId,
                                         @Validated  @RequestBody BomItemDTO b){
         BomItem bomItem = iBomItemService.createBomItem(bomId,b);
         return Result.success("创建明细成功",bomItem);
    }

    //这个itemId指的就是bomItemId
    @PutMapping("/{bomItemId}")
    public Result<BomItem> updateBomItemByBomIdAndItemId(@PathVariable Long bomId,
                                         @PathVariable Long bomItemId,
                                         @RequestBody BomItemDTO b){
        BomItem bomItem = iBomItemService.updateBomItemByBomIdAndBomItemId(bomId,bomItemId,b);
        return Result.success("更新明细成功",bomItem);
    }

    //这里的删除逻辑不只有一条记录
    //还需要递归删除这个子节点下的所有子节点 所以需要写一个递归函数递归删除
    @DeleteMapping("/{bomItemId}")
    public Result<BomItem> deleteBomItemByBomItemId(@PathVariable Long bomId,
                                                    @PathVariable Long  bomItemId){
            BomItem bomItem = iBomItemService.deleteBomItemByBomItemId(bomId,bomItemId);
            return Result.success("删除成功",bomItem);
    }

}
