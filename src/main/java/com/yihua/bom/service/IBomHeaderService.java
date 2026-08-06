package com.yihua.bom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yihua.bom.dto.BomHeaderDTO;
import com.yihua.bom.entity.BomHeader;
import com.yihua.bom.vo.BomHeaderVo;

import java.util.List;
import java.util.Map;

public interface IBomHeaderService extends IService<BomHeader> {
    BomHeader createBomHeader(BomHeaderDTO b);

    IPage<BomHeader> listBomHeader(Long curPage, Long curPageNum, String keyword);

    Map<String, Object> getBomHeaderAndItemsByBomId(Long bomId);

    BomHeader updateBomHeaderByBomId(Long bomId, BomHeaderDTO b);


    Map<String, Object> deleteBomHeaderAndItemByBomId(Long bomId);

    BomHeaderVo activeBomStatus(Long bomId);

    BomHeaderVo disableBomStatus(Long bomId);

    Long getBomHeaderIdByMaterialId(Long materialId);

    Map<String, Object> copyBomHeaderAndBomItemByBomId(Long bomId);

    Long returnDraftBomIdByBomMaterialId(Long materialId);
}
