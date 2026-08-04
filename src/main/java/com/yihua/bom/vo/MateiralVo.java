package com.yihua.bom.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MateiralVo {
    //这个字段用来统计生产指定产品要用到的原材料总数量
    private BigDecimal totalQty;
    private String unit;
    private String materialName;
    private String materialCode;
    private Long materialId;
}
