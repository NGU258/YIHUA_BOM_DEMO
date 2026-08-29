package com.yihua.bom.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yihua.bom.entity.EsopFile;
import com.yihua.bom.service.ITestAllService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/TestAccess")
    public String TestAccess(@RequestBody EsopFile esopFile){
        log.info("接收到的前端请求DTO： {}", JSON.toJSONString(esopFile));
        return "访问成功"+JSON.toJSONString(esopFile)+"传过来的时间格式： "+esopFile.getUrlExpiresAt();
    }
}
