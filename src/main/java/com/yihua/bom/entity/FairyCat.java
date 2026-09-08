package com.yihua.bom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Component //把当前这个类加到spring IOC容器中去 这样@Value注解就能生效了 在需要的位置使用@Autowired注解导入进来就可以了
@JsonInclude(value = JsonInclude.Include.NON_NULL) //由于@RestController在将实体类对象序列化成JSON时默认会保留里面的null值字段 如：{"name":null,"desc":"ft","length":"Test6"} 所以需要使用注解@JsonInclude将所有null值字段忽略掉 不把它放到JSON中 如：{"desc":"ft","length":"Test6"}
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
    @Pattern(regexp = "^[fF].*[tT]$" , message = "不满足以下规则 ： 必须以f或F开头 以t或T结尾 中间任意字符皆可") //当前自定义的规则是必须以f或F开关 以t或T结尾 中间任意字符皆可
    @NotNull(message = "不能为null喵~") //不能为null
//  @NotBlank //不能为null跟空串或空字符串
    private String desc;

    @Length(max=5,message = "length的长度必须<=5")
    private String length;

    //将数字类型转成数字字符串类型 解决前端计算器因数字位数太长导致的精度丢失问题
    //示例：原来序列化的结果是："id":9007199254740993
    //   加了下面这个注解后：  "id":"9007199254740993"
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("userId") //修改该字段在JSON中的字段名 包括序列化跟反序列化场景
    private Long id;

    @JsonIgnore //该字段不参与序列化 换句话说在JSON中不会出现该字段
    private Long height;

    //@ConfigurationProperties支持松散绑定 但@Value注解不支持 里面占位符中的匹配变量名使用的算法乃是精准匹配喵~
    @Value("${fairyCat.name:元气好喵仙~}") //想玩一下松散绑定 但可惜不支持
    private String alias;
}