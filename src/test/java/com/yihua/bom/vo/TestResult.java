package com.yihua.bom.vo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestResult {

    @Test
    public  void testBuilder(){
        System.out.println(Result.success("Good evening~"));
    }
}
