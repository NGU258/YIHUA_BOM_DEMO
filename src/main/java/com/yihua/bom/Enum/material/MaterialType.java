package com.yihua.bom.Enum.material;

import lombok.Getter;

/**
 * 物料类型
 */
@Getter
public enum MaterialType {
    /**
     *成品
     */
    PRODUCT("PRODUCT","成品"),
    /**
     * 半成品
     */
    SEMI_FINISHED("SEMI_FINISHED","半成品"),

    /**
     * 原材料
     */
    RAW_MATERIAL("RAW_MATERIAL","原材料");

    private final String value;
    private final String desc;

    MaterialType(String value,String desc){
        this.value = value;
        this.desc = desc;
    }
}
