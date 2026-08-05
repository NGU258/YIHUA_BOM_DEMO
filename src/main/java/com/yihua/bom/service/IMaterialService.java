package com.yihua.bom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yihua.bom.dto.MaterialDTO;
import com.yihua.bom.entity.Material;
import com.yihua.bom.vo.BomTreeStructVo;
import com.yihua.bom.vo.MaterialVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IMaterialService extends IService<Material> {
    Material createMaterial(MaterialDTO m);

    IPage<Material> listMaterial(Long pageNum, Long count, String key);

    Material getMaterial(Long materialId);

    Material updateMaterial(Long materialId, MaterialDTO mDto);

    Material deleteMaterialById(Long materialId);

    BomTreeStructVo BomTreeStructByMaterialId(Long materialId);

    BomTreeStructVo findBomTreeStructByBomItemId(Long bomId, Long bomItemId);

    Map<Long,MaterialVo> summaryMaterialTotalQty(Long bomId, Long bomItemId,  Map<Long,MaterialVo> materialVoMap, BigDecimal multQty);

    List<MaterialVo> summaryToTalQtyByMaterialId(Long materialId);

    Boolean isRawMaterialByMaterialId(Long materialId);
}
