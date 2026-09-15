package com.yihua.bom.specialCase;

/**
 * 这个类用于处理跟测试在单元测试中没法测试的一些特殊情况
 */
public class TestSpecialCase {

    public static final String clear;

    static{
        clear = "\033[H\033[2J\033[3J";
    }

    public static void main(String[] args) {
        System.out.println("早上好呀~");
        System.out.println(clear); //AI说输出ANSI清屏码可以进行清屏 局限是它只能用在实体类中输出 且需要配置启动项 如果是在单元测试中输出的话会没有效果 但测试了下发现并没有效果
        System.out.flush();
        System.out.println("晚上好呀~");
    }
}
