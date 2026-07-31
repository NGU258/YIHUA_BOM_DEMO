package com.yihua.bom.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Meta表示 用来描述数据的数据
// MetaObject 表示数据的数据对应 对应实体类的话就是比如说： Material类、BomHeader类、BomItem类  这些都可以说是MetaObject的对象
// MetaObjectHanlder 表示专门处理这个对象的类
@Component
public class fairyCatMetaObjectHandler implements MetaObjectHandler {

    //凡是Mybatis-plus在执行插入Sql操作之前 都会来到这个地方 执行对应的插入逻辑
    @Override
    public void insertFill(MetaObject metaObject) {
        //逻辑： 插入时创建时间与更新时间这两个字段都需要赋当前系统的时间

        //使用严格插入方法：   strictInsertFill
        //严格的意思就是会进行类型检查 如果是这个类型则会赋值 反之就不会
        //还有另一个含义就是如果这个Meta对象有这个属性 就会判断类型后再赋值 反之就不会赋值 也不会报错
        //这里需要传四个值
        //第一个值： 传过来的(捕获到的MetaObject实体类对象)
        //第二个值： 要操作的实体类属性名
        //第三个值： 进行类型检查 判断这个实体类属性名的类型是不是当前传入的这个类型
        //第四个值： 如果是的话 就会给前面的这个实体类属性赋值为当前传入的这个值

        //获取当前的系统时间: LocalDateTime.now()

        //给创建时间字段赋值
        strictInsertFill(metaObject,"createTime",LocalDateTime.class,LocalDateTime.now());

        //给更新时间字段赋值
        strictUpdateFill(metaObject,"updateTime",LocalDateTime.class,LocalDateTime.now());

        //这样Mybatis-Plus在执行插入sql的时候就会先给这两个实体类的属性赋上当前的系统时间

    }

    //这里的知识点跟上面的差不多
    @Override
    public void updateFill(MetaObject metaObject) {

        //逻辑： 在更新的时候只需要给更新字段赋值就可以了 创建时间字段只会在插入的时候生效一次

        strictUpdateFill(metaObject,"updateTime",LocalDateTime.class,LocalDateTime.now());

    }
}
