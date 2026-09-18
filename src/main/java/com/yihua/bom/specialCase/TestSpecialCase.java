package com.yihua.bom.specialCase;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

/**
 * 这个类用于处理跟测试在单元测试中没法测试的一些特殊情况
 */
public class TestSpecialCase {

    public static final String clear;

    static{
        clear = "\033[H\033[2J\033[3J";
    }

    //清屏方式一： 输出ANSI
    private static void testANSItoClearScreen(){
        System.out.println("早上好呀~");
        System.out.print(clear); //AI说输出ANSI清屏码可以进行清屏 局限是它只能用在实体类中输出 且需要配置启动项 如果是在单元测试中输出的话会没有效果 但测试了下发现这里也没有效果
        System.out.flush();
        System.out.println("晚上好呀~");
    }

    //清屏方式二：使用Jansi中的工具来清屏
    private static void testJansiToClear(){
        //先安装
        AnsiConsole.systemInstall();
        //然后再调用清屏方法来清空屏幕
        System.out.println("清屏前： 元气小喵仙~");
        AnsiConsole.out().print(Ansi.ansi().eraseScreen().cursor(0,0));
        AnsiConsole.out().flush();
        System.out.println("清屏后： 元气大喵仙~");
        //最后用完了卸载掉
        AnsiConsole.systemUninstall();
    }

    public static void main(String[] args) {

        testJansiToClear();

        testANSItoClearScreen();

        //上面这两种方法在cmd窗口中均验证通过 可以达到预期的清屏效果
        //验证命令： mvn compile exec:java -Dexec.mainClass=com.yihua.bom.specialCase.TestSpecialCase
        //但在Idea的控制台中测试验证发现无效
    }
}
