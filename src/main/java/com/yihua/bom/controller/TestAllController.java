package com.yihua.bom.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yihua.bom.entity.EsopFile;
import com.yihua.bom.entity.FairyCat;
//import com.yihua.bom.entity.FairyCatExcel;
import com.yihua.bom.service.ITestAllService;
import lombok.extern.slf4j.Slf4j;
//import org.hzero.export.annotation.ExcelExport;
//import org.hzero.export.vo.ExportParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/TestAll")
@Slf4j
public class TestAllController {

    @Autowired
    private ITestAllService iTestAllService;

    //2026-08-21T12:30:00+08:00 这个 ISO-8601 的标准格式后面的时区必须加个+空隔 不能是空格 错误写法： 2026-08-21T12:30:00 08:00
    //这里有个坑 在请求URL中里面的+ 会被解码成空格 所以后端在接收的时候其实是不会接收到这个+号的 它传过来的其实是一个空格 就会报解析失败的错误： Text '2026-08-21T12:30:00 08:00' could not be parsed at index 19
    //所以如果想接收空格的话不能只写+  而应该写成%2B 如： 2026-08-21T12:30:00%2B08:00 接收后就是2026-08-21T12:30:00+08:00
    //但如果是通过JSON传过来  JSON字符串里面写成+没啥问题 能正常解析 并不需要写成%2B
    @PostMapping("/testStringReceiveOffsetDateTime")
    public String testStringReceiveOffsetDateTime(@RequestParam("dateTime1") String dateTime1 , @RequestParam("dateTime2") String dateTime2){
        return iTestAllService.testStringReceiveOffsetDateTime(dateTime1,dateTime2);
    }

    @GetMapping("/testAccess")
    public String TestAccess(@RequestBody EsopFile esopFile){
        log.info("接收到的前端请求DTO： {}", JSON.toJSONString(esopFile));
        return "访问成功"+JSON.toJSONString(esopFile)+"传过来的时间格式： "+esopFile.getUrlExpiresAt();
    }

    //http请求中传递的参数所在位置就两个地方  一个是在URL路径上(参数与路径用?隔开，参数之间用&隔开)  一个是在请求体JSON中
    //未完待续
    @GetMapping("/testsAccessWay")
    public String TestAccessWay(String name, int age, FairyCat cat,@RequestParam("desc") String desc){
        return "name:"+name+" age:"+age +" cat: "+JSON.toJSONString(cat,SerializerFeature.WriteMapNullValue)+" desc:"+desc;
    }

    //测试导出功能的实现
//    @GetMapping("/testExportFunction")
//    //导出功能实现 可以参考hlos-mds项目中AndonClassController的 export接口方法
//    //一： 在接口前面加个@ExcelExport注解  这样后端返回的数据就会以流的形式返回给前端
//    @ExcelExport(value = FairyCatExcel.class) //注解里面的value属性值： 传一个自定义类的class进来
//    //二： 在自定义类中添加两个注解来绑定实体类跟实体类中的属性 具体看FairyCatExcel
//    public String testExportFunction(FairyCatExcel fairyCatExcel,
//                                     ExportParam exportParam,//三： 最后再添加两个必须接收的属性 一个是导出参数类ExportParam  另一个是用于获取下载流的类HttpServletResponse
//                                     HttpServletResponse response){
//
//        //配合AI的原理：导出功能的机制是注解驱动的 AOP切面
//        //@ExcelExport 的切面在拦截到接口方法返回值后
//        //通过自定义类中的 @ExcelSheet、@ExcelColumn 来解析出sheet结构、列头、列顺序，之后把数据一行行填进去
//        //最后再写入 HttpServletResponse 输出给浏览器下载
//        //注意这里的话只会生成一个sheet 而且只有被@ExcelColumn注解修辞的字段名才会被导出来
//        return "导出功能实现";
//    }

    //测试一下汉得老项目中的校验注解
    @GetMapping("/testValidation")
    public String testValidatio(@Validated  @RequestBody FairyCat fairyCat){
        System.out.println(JSON.toJSONString(fairyCat,SerializerFeature.WriteMapNullValue));
        return JSON.toJSONString(fairyCat,SerializerFeature.WriteMapNullValue);
    }
}
