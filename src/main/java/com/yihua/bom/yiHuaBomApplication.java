package com.yihua.bom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//Mybatis-Plus的自动配置会自动扫描@SpringBootApplication类所在包中的所有子包 所以可以不手动指定@MapperScan注解
//这里可以看到包路径是 com.yihua.bom 如果不手动指定@MapperScan的话 这里默认就是@MapperScan("com.yihua.bom")
@SpringBootApplication
public class yiHuaBomApplication {
    public static void main(String[] args) {
        SpringApplication.run(yiHuaBomApplication.class, args);
    }
}
