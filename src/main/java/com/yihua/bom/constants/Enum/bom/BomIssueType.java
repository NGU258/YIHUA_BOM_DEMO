package com.yihua.bom.constants.Enum.bom;

import lombok.Getter;

/**
 * 投料方式
 */
@Getter
public enum BomIssueType {

    /**
     * 正常投料
     */
    NORMAL("NORMAL","正常投料"),

    /**
     * 倒冲
     */
    BACKFLUSH("BACKFLUSH","倒冲"),

    /**
     * 手工投料
     */
    MANUAL("MANUAL","手工投料");

    BomIssueType(String value,String desc){
        this.value = value;
        this.desc = desc;
    }

    private final String value;
    private final String desc;

}
