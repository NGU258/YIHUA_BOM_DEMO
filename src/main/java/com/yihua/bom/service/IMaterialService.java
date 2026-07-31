package com.yihua.bom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yihua.bom.dto.MaterialDTO;
import com.yihua.bom.entity.Material;

public interface IMaterialService extends IService<Material> {
    Material createMaterial(MaterialDTO m);
}
