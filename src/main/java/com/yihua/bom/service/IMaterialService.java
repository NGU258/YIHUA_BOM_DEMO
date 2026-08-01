package com.yihua.bom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yihua.bom.dto.MaterialDTO;
import com.yihua.bom.entity.Material;

public interface IMaterialService extends IService<Material> {
    Material createMaterial(MaterialDTO m);

    IPage<Material> listMaterial(Long pageNum, Long count, String key);

    Material getMaterial(Long materialId);

    Material updateMaterial(Long materialId, MaterialDTO mDto);

    Material deleteMaterialById(Long materialId);
}
