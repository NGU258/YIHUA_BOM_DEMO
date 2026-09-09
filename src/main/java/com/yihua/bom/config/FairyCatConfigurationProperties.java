package com.yihua.bom.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(FairyCatConfigurationProperties.alias) //表示该类是一个配置类 该类的属性名会自动绑定前缀prefix字段值所在yml配置文件中对应的所有属性 支持松散绑定  名字建议使用SpringBoot官方推荐的烤肉串命名
@Component //需要加该注解将该类的Bean添加到IOC容器中 以触发ConfigurationProperties注解对应的绑定动作 不加的话编译器就会报错
public class FairyCatConfigurationProperties {

    private String Nam_E; //没想到实体类字段名也支持松散绑定

    private Long age;

    public static final String alias = "f-a-i-r-y-c-a-t";

    @Value("生成的cuid: ${random.cuid}")
    private String desc;

    private Character sex;
}
//玩法在TestAll单元测试中

