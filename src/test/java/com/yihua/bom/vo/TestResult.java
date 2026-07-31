package com.yihua.bom.vo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestResult {

    @Test
    public  void testBuilder(){
        System.out.println(Result.success("Good evening~"));
    }

    @Test
    public void testResult(){
        System.out.println(Result.success("元气小喵仙~"));
        System.out.println(Result.fail("元气小喵仙~"));
    }


}
