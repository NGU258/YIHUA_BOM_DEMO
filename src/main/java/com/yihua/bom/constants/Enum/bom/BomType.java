package com.yihua.bom.constants.Enum.bom;

import lombok.Getter;

//使用枚举类的好处就是类型安全 有相关的校验
//另一个就是直观  直接.调用的时候不会像常量类那样把所有的不同类的选项都列出来
// 第三个好处是它自带一些方法 比如values方法 会返回所有枚举值 以下面这个类为例 返回的就是List<BomType>
@Getter
public enum BomType {

    EBOM("EBOM"), //这些要写在最前面
    MBOM("MBOM"),
    PBOM("PBOM"); //最后以分号结尾

    private final String value; //默认常识： 枚举值是不可变的常量 所以需要用final修辞
    //在枚举类中构造方法默认不写就是private
    BomType(String value){
        this.value = value;
    }
}
