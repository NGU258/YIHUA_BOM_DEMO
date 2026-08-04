package com.yihua.bom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yihua.bom.dto.MaterialDTO;
import com.yihua.bom.entity.Material;
import com.yihua.bom.vo.BomTreeStructVo;
import com.yihua.bom.vo.MateiralVo;

import java.util.List;

public interface IMaterialService extends IService<Material> {
    Material createMaterial(MaterialDTO m);

    IPage<Material> listMaterial(Long pageNum, Long count, String key);

    Material getMaterial(Long materialId);

    Material updateMaterial(Long materialId, MaterialDTO mDto);

    Material deleteMaterialById(Long materialId);

    BomTreeStructVo BomTreeStructByMaterialId(Long materialId);

    BomTreeStructVo findBomTreeStructByBomItemId(Long bomId, Long bomItemId);

    List<MateiralVo> summaryToTalQtyByMaterialId(Long materialId);
}
