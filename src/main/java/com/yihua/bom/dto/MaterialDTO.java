package com.yihua.bom.dto;

import lombok.Builder;
import lombok.Data;

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
