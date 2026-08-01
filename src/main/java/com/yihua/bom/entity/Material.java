package com.yihua.bom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data //实现get/set方法 重写toString hashCode equals等方法
@Builder //使用建造者模式！这里有一个坑(Builder注解会自动生成全参构造  这样的话无参构造就不会自动生成出来了 就会导致new Material()的时候报错了)
@TableName("material")
@NoArgsConstructor //这里生成了一个无参构造方法之后 @Builder就不会自动生成全参构造了 这个和java的默认无参构造方法的生成原理是一样的 如果有就不生成 如果没有才会生成
@AllArgsConstructor
public class Material {

    //value告诉Mybatis-plus这个字段在数据库里的名字叫啥 AUTO表示主键自增 默认是NONE
    @TableId(value = "id" , type = IdType.AUTO)
    private String id;

    //value这个属性比较特殊 如果只有一个value需要赋值的话value可以省略不写
    @TableField("material_code")
    private String materialCode;

    @TableField("material_name")
    private String materialName;

    @TableField("material_type")
    private String materialType;

    @TableField("spec")
    private String spec;

    @TableField("unit")
    private String unit;

    @TableField("enabled")
    private String enabled;

    //这里注意不能使用exists = false
    //不然的话进行查询的时候查询列里面就不会出现这列 这样的话就导致接收的java对象中会没有这一属性的值
    //虽然数据库中是有值的 但这样操作的话就读取不到对应数据库中的值了
    //最佳实践应该是 通过fill = FillField.insert 来告诉Mybatis-Plus这里需要进行自动填充 但这里只是一个标记
    //然后再通过实现了MetaObjectHandler接口的子类里面的方法来执行自动填充逻辑

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic //告诉Mybatis-plus这个是逻辑删除字段 后面的删除逻辑会变成把deleted字段值更新成1
    //由于数据库默认值不会进行回填 所以返回给前端的时候这里的值就是null
    private Integer deleted ;
}
