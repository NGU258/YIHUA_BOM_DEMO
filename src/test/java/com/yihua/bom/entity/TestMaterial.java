package com.yihua.bom.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
public class TestMaterial {
    @Test
    public void testMaterialBuilder(){

        Material m1 = Material.builder()
                .materialCode("fairyCa001")
                .materialName("成品A")
                .materialType("标准")
                .build();

        System.out.println(m1.toString());
    }

    //测试正则表达式
    @Test
    public void testZZ001(){
        String myRules = "^a[a-z]{2,4}";//匹配以a开头后面接2-4个小写字母

        Pattern compile = Pattern.compile(myRules);
        Matcher a1 = compile.matcher("aa");//后面a的长度只有1个 不满足
        Matcher a2 = compile.matcher("aaa"); //长度有2个 满足
        Matcher a3 = compile.matcher("aaaa"); //长度有3个 也满足

        System.out.println(a1.matches());
        System.out.println(a2.matches());
        System.out.println(a3.matches());
    }

    @Test
    public void testZZ002(){
        String orginStr = "Duplicate entry 'BOM-A-001' for key 'material.uk_material_code'";
        //我需要捕获后面的uk_materail_code

        String result;
        // split的形参其实是正则表达式
        //所以需要将.转换成对应的普通字符 也就是加上转义\\
        for(String cur : orginStr.split("\\.")){
//            System.out.println(cur);
            if(cur.contains("uk_")){

                //这里用贪婪与非贪婪模式都可以
                Matcher matcher1 = Pattern.compile("uk_(.+?)'").matcher(cur);

                //这里需要调用find或matches方法来执行实际的匹配操作
                //如果直接调用的话就会抛出异常，提示信息： No match found
                if (matcher1.find()) {
                    String fieldName = matcher1.group(1); //拿第一个捕获组中的数据
                    System.out.println("字段名： "+fieldName);
                }
            }
        }

        //前面的这个.*?就得使用非贪婪模式了 不然的话就直接吃到后面了
        Matcher matcher2 = Pattern.compile(".*?'(.+?)'").matcher(orginStr);

        if(matcher2.find()){
            String fieldName = matcher2.group(1);
            System.out.println("字段值名： "+fieldName);
        }

        Pattern pattern1 = Pattern.compile(".*-([a-zA-Z0-9]+)");
        Pattern pattern2 = Pattern.compile(".*-(\\d*)");
        //在正则表达式中 \d表示匹配任意一个数字 等价[0-9]
        //Java中String的format方法 等价于C中的printf方法 语法几乎一样
        Matcher matcher1 = pattern1.matcher("BOM-A-0001");
        if(matcher1.find())
            System.out.println("截取的值： "+matcher1.group(1));
        Matcher matcher3 = pattern2.matcher("BOM-A-");
        //这里测试发现如果group没有捕获到的话会返回一个空字符串""
        if(matcher3.find())
            System.out.println("使用\\d截取的值： "+matcher3.group(1)+ StringUtils.hasText(matcher3.group(1))+(matcher3.group(1).equals("")));



    }

    //测试贪婪模式.+与非贪婪模式.+?
    //最主要的区别： 贪婪模式会尽可能多的匹配，而非贪婪模式则是尽可能少的匹配。
    @Test
    public void testGreedyModel(){

        String str = "uk_a'b'c'd'";  //这里有多个单引号

        // 贪婪模式：.+ 会尽可能多的吞掉字符~ 直到遇到最后一个 '就不吞了
        Pattern greedy = Pattern.compile("uk_(.+)'");
        Matcher m1 = greedy.matcher(str);
        if (m1.find()) {
            System.out.println("贪婪模式(.+) : " + m1.group(1));
            // 输出: a'b'c'd
        }

        // 非贪婪模式：.+? 只要满足一次就不会继续往后走了 也就是吞到第一个 ' 就停
        Pattern lazy = Pattern.compile("uk_(.+?)'");
        Matcher m2 = lazy.matcher(str);
        if (m2.find()) {
            System.out.println("非贪婪模式(.+?): " + m2.group(1));
            // 输出: a
        }
    }

    @Test
    public void testStringFormat(){
        String testStr = "BOM-A-0001";

        System.out.println(testStr);

        String newTestStr = testStr.replace("0001","0002");
        System.out.println(testStr);

        System.out.println(newTestStr);

        System.out.println(String.format("%04d",2));
    }

    @Test
    public void testZZLambda(){
        Matcher matcher = Pattern.compile("(\\d+)$").matcher("BOM-半成品B-v1");
        if(matcher.find())
            System.out.println(matcher.group(1));
    }

    @Test
    public void testReplace(){
        String orginStr = "Good morning~";
        orginStr = orginStr.replace("66","00");
        System.out.println(orginStr);

    }


}

