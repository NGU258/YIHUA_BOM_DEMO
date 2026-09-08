package com.yihua.bom.TestAll;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yihua.bom.config.FairyCatConfigurationProperties;
import com.yihua.bom.entity.EsopFile;
import com.yihua.bom.entity.FairyCat;
import com.yihua.bom.entity.Step;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
        //测试去掉末尾的逗号,
        StringBuilder sb = new StringBuilder("abc,");
        System.out.println(sb.toString().substring(0,sb.length()-1));

        //测试清空字符串
        sb.setLength(0);//长度归零后StringBuilder中的原字符数组字符将会被后面拼接过来的字符覆盖掉
        sb.append("元气小喵仙~").append("晚上好喵~");
        System.out.println(sb.toString());
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

    //测试StringUtils类里面的补零方法leftPad
    @Test
    public void testLeftPad(){
        String test = "1";

        //补3位
        //注意这里第三个参数要传一个字符过去  不能传一个字符串 不然调用的重载方法会不一样就会导致报错了
        //参数列表： 被操作的字符串 总长度 占位符
        //含义： 当被操作的字符串长度小于总长度时，左边剩余的部分替换成指定占位符
        String result = StringUtils.leftPad(test,4,'0');
        System.out.println("补零的结果： "+result);

        //第二种方法(使用String类自带的format方法 但前提这个字符串要是数字 不然会抛出异常)
        //这里如果想补零的前提是这个字符串要是数字字符串 且只能用转成数字后用%04d来输出 无法用%s来控制 它不支持补零操作
        String result2 = String.format("%04d",Integer.parseInt(test));
        System.out.println("使用format补零的结果： "+result2);
    }

    //测试StringUtils工具类中的方法
    @Test
    public void testStringUtilsFun(){
        //默认值
        //只有第一个参数为null时则使用默认值（第二个参数） 反之如果是空字符串或其它非空字符串都会直接原样返回
        System.out.println(StringUtils.defaultString("","666"));
        System.out.println(StringUtils.defaultString(null,"元气小喵仙"));
    }


    //测试自然排序比较器
    @Test
    public void testNaturalOrderComparator(){

        //测试一下请求与响应的时间
        long start = System.currentTimeMillis();
        List<Step> stepList = new ArrayList<>();
        stepList.add(Step.builder()
                .operationStepNum(3l)
                .operationStepName("C")
                .build());
        stepList.add(Step.builder()
                .operationStepNum(null)
                .operationStepName("D")
                .build());
        stepList.add(Step.builder()
                .operationStepNum(1l)
                .operationStepName("A")
                .build());
        stepList.add(Step.builder()
                .operationStepNum(2l)
                .operationStepName("B")
                .build());

        System.out.println("排序前(使用JSONArray类方法序列化成JSON)： ");
        System.out.println(JSONArray.toJSONString(stepList));
        System.out.println("排序后(使用JSONArray类方法序列化成JSON)： ");
        stepList.sort(Comparator.comparing(Step::getOperationStepNum,Comparator.nullsLast(Comparator.naturalOrder())));
        System.out.println(JSONArray.toJSONString(stepList));

        System.out.println("排序前(使用JSON类方法序列化成JSON)： ");
        System.out.println(JSON.toJSONString(stepList));
        System.out.println("排序后(使用JSON类方法序列化成JSON)： ");
        stepList.sort(Comparator.comparing(Step::getOperationStepNum,Comparator.nullsLast(Comparator.naturalOrder())));
        System.out.println(JSON.toJSONString(stepList));


        System.out.println("测试对对象进行序列化操作：");
        //默认null字段是不会被序列化进去输出的
        System.out.println(JSON.toJSONString(Step.builder()
                //.operationStepNum(1l)
                //.operationStepName("元气小喵仙~")
                .build()));

        System.out.println("测试序列化时输出null字段");
        //如果想输出的话得加个序列化特性开关SerializerFeature
        System.out.println(JSON.toJSONString(Step.builder()
                .build(), SerializerFeature.WriteMapNullValue));

        //除了这个还有其它好玩的东西
        //WriteMapNullValue 所有null字段都输出 示例： "name":null
        //WriteNullStringAsEmpty 所有null字段都输出为空字符串 示例： "name":""
            //测试一下null转空字符串
            System.out.println("测试一下null转空字符串"); //测试发现Object对象这里没法null转""
            System.out.println(JSON.toJSONString(Step.builder()
                .build(), SerializerFeature.WriteNullStringAsEmpty));

        //WriteNullNumberAsZero 所有null字段都输出为0 示例： "age":0
        //WriteNullListAsEmpty 所有null字段都输出为空列表 示例： "stepList":[]
        //WriteNullBooleanAsFalse 所有null字段都输出为false 示例： "enabled":false

        System.out.println("响应时间： "+ (System.currentTimeMillis() - start ));

        //测试一下按文件名默认升序
        List<EsopFile> stepList666 = new ArrayList<>();
        stepList666.add(EsopFile.builder()
                .fileSize(6l)
                .fileName("C")
                .build());
        stepList666.add(EsopFile.builder()
                .fileSize(null)
                .fileName("D")
                .build());
        stepList666.add(EsopFile.builder()
                .fileSize(6l)
                .fileName("A")
                .build());
        stepList666.add(EsopFile.builder()
                .fileSize(6l)
                .fileName("B")
                .build());


        System.out.println("没按文件名排序前【EsopFile】： "+JSON.toJSONString(stepList666));

        stepList666.sort(Comparator.comparing(cur->cur.getFileName(),Comparator.nullsLast(Comparator.naturalOrder())));
        System.out.println("按文件名排序后【EsopFile】： "+JSON.toJSONString(stepList666));

        //测试一下将非PDF文件的对象过滤掉
        List<EsopFile> stepList667 = new ArrayList<>();
        stepList667.add(EsopFile.builder()
                .fileType("PDF")
                .fileSize(6l)
                .fileName("C")
                .build());
        stepList667.add(EsopFile.builder()
                .fileType("DOCX")
                .fileSize(null)
                .fileName("D")
                .build());
        stepList667.add(EsopFile.builder()
                .fileType("pdf")
                .fileSize(6l)
                .fileName("A")
                .build());
        stepList667.add(EsopFile.builder()
                .fileType("img")
                .fileSize(6l)
                .fileName("B")
                .build());

        System.out.println("过滤前： "+JSON.toJSONString(stepList667));
        List<EsopFile> typeResult = stepList667
                .stream() //把当前数组变成一个流水线  里面的元素将会一个个的从流水线中流出来
                .filter(cur -> "PDF".equalsIgnoreCase(cur.getFileType())) //过滤器设置筛选规则  只有满足条件的元素才会被留下来 参数是lambda表达式 示例： cur->布尔值
                .collect(Collectors.toList());//将所有满足条件的元素重新装进一个新的数组 保存完后最终返回
        System.out.println("过滤后(使用流式写法)： "+JSON.toJSONString(typeResult));//验证通过
        //下面的这个传统写法等价于上面的流式写法
        List<EsopFile> esopFileList = new ArrayList<>();
        for(EsopFile cur: stepList667)
            if("PDF".equalsIgnoreCase(cur.getFileType()))
                esopFileList.add(cur);
        System.out.println("过滤后(使用传统写法)： "+JSON.toJSONString(esopFileList));
    }

    //测试一下自定义配置项
    @Autowired
    FairyCat fairyCatTest;
    @Test
    public void testBindLazy(){
        System.out.println(JSON.toJSONString(fairyCatTest));
    }

    //测试一下配置类字段名跟yml配置文件属性名之间的映射绑定
    @Autowired
    private FairyCatConfigurationProperties fairyCatConfigurationProperties;
    @Test
    public void testBindYml(){
        System.out.println(JSON.toJSONString(fairyCatConfigurationProperties));
    }
}
