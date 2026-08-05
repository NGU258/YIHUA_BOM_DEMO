package com.yihua.bom.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterialVo {
    //这个字段用来统计生产指定产品要用到的原材料总数量
    private BigDecimal totalQty;
    private String unit;
    private String materialName;
    private String materialCode;
    private Long materialId;
}
