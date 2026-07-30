package com.yihua.bom.Enum.bom;

import lombok.Getter;

@Getter
public enum BomStatus{
    DRAFT("DRAFT","草稿"),
    ACTIVE("ACTIVE","启用"),
    DISABLED("DISABLED","停用");

    BomStatus(String value,String desc){
        this.value = value;
        this.desc = desc;
    }
    private final String value;
    private final String desc;

}
