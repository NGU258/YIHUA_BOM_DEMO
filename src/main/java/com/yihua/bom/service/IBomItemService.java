package com.yihua.bom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yihua.bom.dto.BomItemDTO;
import com.yihua.bom.entity.BomItem;
import com.yihua.bom.vo.BomItemVo;

import java.util.List;

public interface IBomItemService extends IService<BomItem> {
    List<BomItem> getDetailsById(Long bomId);

    BomItem createBomItem(Long bomId, BomItemDTO b);

    BomItem updateBomItemByBomIdAndBomItemId(Long bomId, Long bomItemId, BomItemDTO b);

    BomItemVo deleteBomItemByBomItemId(Long bomId,Long bomItemId);

    BomItemVo findBomItemTreeStructByBomItemId(Long bomItemId);

    BomItemVo deleteBomItemTreeStructByBomItemId(Long bomItemId);

}
