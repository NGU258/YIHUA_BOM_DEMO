package com.yihua.bom.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Component //把当前这个类加到spring IOC容器中去 这样@Value注解就能生效了 在需要的位置使用@Autowired注解导入进来就可以了
public class FairyCat {

    //好玩的给成员变量赋默认值的用法： @Value注解+占位符
    //  @Value("${...}") 表示从 Spring 配置（application.yml / nacos / 环境变量等）中取对应的value赋值给当前的成员变量。
    //  ${key:defaultValue} 中defaultValue可以进行嵌套 写法上也不会区别是数字还是字符串(因此这里写字符串时不需要加引号)
    //  它在注入的时候会自动根据当前字段类型进行推断
    //  原理如下：
        // ① 首先配置文件yml中的字面量value会被 SnakeYAML 解析（根据YAML 规范推断字面量类型，只会解析一次）
        // ② 但这些value会被统一当成字符串读进Environment  所以在配置文件中字符串字面量要不要双引号也无所谓
        // ③ @Value在注入的时候，会使用ConversionService来做字符串转成目标类型的转换工作
        // ④ 若转换失败启动就会报错

    //注意事项
        //1. 注解所在的包路径是org.springframework.beans.factory.annotation 并不是在lombok路径中的
        //2. 它只服务于在Spring容器中的对象  如果是自己手动new出来的是不会生效的 对应的字段值还是null
    @Value("${spring.datasource.username:元气小喵仙~}")
    private String name;

    @Value("${JAVA_HOME:环境变量中没有这个值喵~}")
    private String desc;
}
