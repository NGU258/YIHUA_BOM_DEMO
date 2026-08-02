package com.yihua.bom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yihua.bom.dto.BomItemDTO;
import com.yihua.bom.entity.BomItem;

import java.util.List;

public interface IBomItemService extends IService<BomItem> {
    List<BomItem> getDetailsById(Long bomId);

    BomItem createBomItem(Long bomId, BomItemDTO b);

    BomItem updateBomItemByBomIdAndBomItemId(Long bomId, Long bomItemId, BomItemDTO b);

    BomItem deleteBomItemByBomItemId(Long bomId,Long bomItemId);
}
