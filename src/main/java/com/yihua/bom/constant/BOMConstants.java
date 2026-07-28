package com.yihua.bom.constant;

/**
 * 这个类主要是存放关于BOM的一些常量值
 * @author 罗永庆
 * @data 2026/7/16
 */
public final class BOMConstants {

    private BOMConstants(){

    }

    /**
     * BOM类型： EBOM
     */
    public static final String BOM_TYPE_EBOM = "EBOM";
    /**
     * BOM类型： MBOM
     */
    public static final String BOM_TYPE_MBOM = "MBOM";
    /**
     * BOM类型： PBOM
     */
    public static final String BOM_TYPE_PBOM = "PBOM";

    /**
     * 状态： 草稿
     */
    public static final String STATUS_DRAFT = "DRAFT";
    /**
     * 状态：启用
     */
    public static final String STATUS_ACTIVE = "ACTIVE";
    /**
     * 状态： 停用
     */
    public static final String STATUS_DISABLED = "DISABLED";

    /**
     * 投料方式： 正常投料
     */
    public static final String ISSUE_TYPE_NORAML = "NORMAL";
    /**
     * 投料方式： 倒冲
     */
    public static final String ISSUE_TYPE_BACKFLUSH = "BACKFLUSH";
    /**
     * 投料方式： 手工投料
     */
    public static final String ISSUE_TYPE_MANUAL = "MANUAL";


}
