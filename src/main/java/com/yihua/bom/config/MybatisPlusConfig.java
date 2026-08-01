package com.yihua.bom.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration的作用是告诉 Mybatis-Plus当前类是一个配置类 里面可能需要创建对应的Bean
@Configuration
public class MybatisPlusConfig {


    //@Bean注解的作用是给当前方法的返回值对象创建一个对应的Bean 然后交给IOC容器管理
    //后面其它类可以通过依赖注入把这个Bean依赖进来使用
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){

        //Pagination  中文： 页码、分页的意思
        //注意Mybatis-Plus在生成SQL的时候真正给后面的SQL加limit ?,?的是拦截器这里
        //而分页插件里面的selectPage方法并不会给生成的SQL加上后面的这个limit?,?
        //如果这里不指定的话最终查询的数据还是所有的数据 并不会进行分页

        //首先创建一个Mybatis-Plus的拦截器容器 里面存储的是相应的拦截器
        MybatisPlusInterceptor fairyCatInterceptor = new MybatisPlusInterceptor();

        //然后把分页拦截器给加进这个容器里面
        //这里指定使用Mysql的数据库语言 这样它生成对应的分页语句的时候就会用Mysql的语法去生成了
        //因为不同的数据库 它们的分页语法也会有差异
        fairyCatInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return fairyCatInterceptor;

    }
}
