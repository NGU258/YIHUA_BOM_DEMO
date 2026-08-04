package com.yihua.bom.dto;

import lombok.Builder;
import lombok.Data;

//测试数据
//{
//        "materialCode":"fairyCat007",
//        "materialName":"电池",
//        "materialType":"raw_material",
//        "spec":"Testgc",
//        "unit":"组"
//        }

@Data
@Builder
public class MaterialDTO {

    private String materialCode;

    private String materialName;

    private String materialType;

    private String spec;

    private String unit;

    //默认启用吧
    private Integer enalbed = 1;

}
