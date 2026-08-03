package com.yihua.bom.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class BomItemVo {

    private Long id;

    private Long bomId;

    private Long parentId;

    private BigDecimal qty;

    private String unit;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String materialSpec;

    private Integer itemNo;

    private BigDecimal lossRate;

    private BigDecimal fixedLossQty;

    private String issueType;

    private String processCode;

    private String processName;

    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer deleted = 0;

    private List<BomItemVo> childNode  = new ArrayList<>();

}
