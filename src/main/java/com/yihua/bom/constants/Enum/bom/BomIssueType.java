package com.yihua.bom.constants.Enum.bom;

import lombok.Getter;

/**
 * 投料方式
 */
@Getter
public enum BomIssueType {

    /**
     * 正常投料
     *意思： 工作拿工单去仓库领料
     */
    NORMAL("NORMAL","正常投料"),

    /**
     * 倒冲
     *做完之后系统会按照标准用量自动扣掉库存
     * 因为比如说把油漆放到喷枪里面 然后用了多少是没法精确计算的 所以系统会按照标准用量来扣除
     */
    BACKFLUSH("BACKFLUSH","倒冲"),

    /**
     * 手工投料
     *纯人工来进行记录用料
     */
    MANUAL("MANUAL","手工投料");

    BomIssueType(String value,String desc){
        this.value = value;
        this.desc = desc;
    }

    private final String value;
    private final String desc;

}
