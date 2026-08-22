package com.yihua.bom.TestAll;

import com.yihua.bom.entity.FairyCat;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringBootTest //加了这个注解代表这个类是测试类 同时它也会被spring IOC容器所管控
@Slf4j
public class TestAll {

    //使用Java自带的工具类 Collections 里面全是静态方法
    //测试单例列表 singletonList
    @Test
    public void testSingletonList(){

        List<Long> list = new ArrayList<>();
        list.add(1l);
        list.add(2l);

        //ArrayList是可变的 而singletonList是不可变的
        //singletonList的特点
        //1. 不可变性 它可以读 但不能增删改 如果操作的话会直接抛出异常UnsupportedOperationException
        //方法的内部实现是： 永远只会存一个元素 就像一个托盘只放一只碗一样 它比较适用于传入后只读的业务场景
            //这里也就是把单个值1包装成只有一个元素的只读列表 也叫单元素列表
        List<Integer> intList = Collections.singletonList(1);

        //这里的对象也可以是一个List
        List<List<Long>> lists = Collections.singletonList(list);
        List<Long> list1 = Arrays.asList(666l, 777l, 888l); //asList返回的数组内部是固定长度数组 所以不支持增删操作
        //lists.add(list1); 因为不可变性 无法添加

        System.out.println("遍历singletonList中存储的所有子数组");
        lists.forEach(curList->{
            curList.forEach(cur->{
                System.out.print(cur+" ");
            });
            System.out.println();
        });

        System.out.println("遍历SingletonList数组:");
        intList.forEach(cur->{
            System.out.println(cur);
        });
    }

    //测试日志输出方法
    @Test
    public void testLog(){
        log.debug("测试日志输出");
    }

    //测试Value注解的玩法
    @Autowired
    private FairyCat cat;
    @Test
    public void testValue(){
        //FairyCat  cat = new FairyCat(); 不能用这种方法创建对象 因为它是不受spring管控的
        System.out.println(cat.getName()); //输出root
        System.out.println(cat.toString());//输出FairyCat(name=root, age=null)
    }

    //测试在测试类中使用构造器注入的方式
    private final FairyCat fairyCat;

    //注意测试类的实例是由JUnit5来创建的 而不是由Spring来创建的
    //而且JUnit5默认不会去解析Spring的构造器参数 不过@Autowired字段注入不受影响
    //如果想保留这种构造器注入的形式 可以在构造器参数的前面加@Autowired注解来进行注入
    public TestAll(@Autowired FairyCat fairyCat){
        this.fairyCat = fairyCat;
    }
    @Test
    public void testValueAnnotation(){
        System.out.println(fairyCat);
    }

    //测试forEach传null会不会报空指针异常
    @Test
    public void testForEach(){

        //是null的话会报空指针异常
        //因为for-each循环编译后其实是调用对象的迭代器来遍历的 会调用iterator方法 所以会报空指针异常
        List<String> strList = null;

        //防御性编程 赋值成一个空列表(单例、不可变)
        strList = strList == null ? Collections.emptyList() : strList;

        for(String cur: strList)
            System.out.println(cur);
    }

    @Test
    public void testStringBuilder(){
        StringBuilder sb = new StringBuilder("abc,");
        System.out.println(sb.toString().substring(0,sb.length()-1));
    }

    @Test
    public void testEqualsIgnoreCase(){
        String test = "S";
        System.out.println(test.equalsIgnoreCase("s"));
    }

    //测试括号是否被当成普通字符
    @Test
    public void testC(){
        System.out.println("()");
    }

    //测试生成UUID
    @Test
    public void testUUID(){

        String uuidStr = UUID.randomUUID().toString();

        System.out.println("原uuid: "+ uuidStr);

        //去掉UUID中的-
        String newUUID = uuidStr.replace("-","");
        System.out.println("新uuid: "+newUUID);

    }


    //测试获取当前的日期与时间
    @Test
    public void testGetCurrentDate(){
        System.out.println("当前时间：" + LocalDateTime.now());
    }

    //对LocalDateTime返回的日期格式进行自定义化
    @Test
    public void testLocalDateTimeFormat(){
        LocalDateTime today = LocalDateTime.now();

        System.out.println("今天的日期与时间： "+today);

        //1. 先定义一个日期格式化器(翻译官)
        //1.1 调用静态方法ofPattern来设置自定义日期格式
        //1.2 日期格式小知识
            //1.2.1 yyyy 代表4位年份 如2026
            //1.2.2 MM 代表2位月份   如08
            //1.2.3 dd 代表2位日期  如12
            //1.2.4 HH 代表24小时制的小时 如下午3点是15
            //1.2.5 mm 代表2位分钟 如08
            //1.2.6 ss 代表2位秒钟 如08
            //1.2.7 SSS 代表3位毫秒 如001
            //1.2.10 E 代表星期几
        DateTimeFormatter localDateTimeFormat = DateTimeFormatter.ofPattern("yyyy年MM月dd日 E HH时mm分ss秒 ");
        //2. 然后链式调用里面的format方法 把之前的日期格式化器传进来就可以了  它返回的是一个字符串
        String todayNew = LocalDateTime.now().format(localDateTimeFormat);
        System.out.println("格式化后的日期与时间： "+todayNew);
    }
}
