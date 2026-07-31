package com.yihua.bom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data //实现get/set方法 重写toString hashCode equals等方法
@Builder //使用建造者模式！
@TableName("material")
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
    private String createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

    @TableField("deleted")
    @TableLogic //告诉Mybatis-plus这个是逻辑删除字段 后面的删除逻辑会变成把deleted字段值更新成1
    private String deleted;
}
