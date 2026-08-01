package com.yihua.bom.controller;

import com.yihua.bom.dto.BomItemDTO;
import com.yihua.bom.entity.BomItem;
import com.yihua.bom.service.IBomItemService;
import com.yihua.bom.vo.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boms")
public class BomItemController {

    private final IBomItemService iBomItemService;

    public BomItemController(IBomItemService ibis){
        this.iBomItemService = ibis;
    }

    @PostMapping("/{bomId}/items")
    public Result<BomItem> createBomItem(@PathVariable Long bomId,
                                         @RequestBody BomItemDTO b){
         BomItem bomItem = iBomItemService.createBomItem(bomId,b);
         return Result.success("插入明细成功",bomItem);

    }

}
