package com.yihua.bom.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BomTreeStructVo {

    private BigDecimal qty;

    private String unit;

    private String materialName;

    private String materialCode;

    private Long materialId;

    @Builder.Default
    private List<BomTreeStructVo> childNode = new ArrayList<>();
}
