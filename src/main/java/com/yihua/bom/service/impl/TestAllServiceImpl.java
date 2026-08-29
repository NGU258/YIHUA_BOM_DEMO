package com.yihua.bom.service.impl;

import com.yihua.bom.service.ITestAllService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@Slf4j
public class TestAllServiceImpl implements ITestAllService {

    @Override
    public String testStringReceiveOffsetDateTime(String dateTime1, String dateTime2) {
        log.info("接口已经访问");
        return "OffsetDateTime: " + dateTime1 + " String: " + dateTime2 + " 转换成功： "+ OffsetDateTime.parse(dateTime1); //parse方法将当前字符串转成OffsetDateTime类型
    }
}
