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
    @Min(value = 1,message = "spec必须大于0喵~")
    private String spec;

    @TableField("unit")
    private String unit;

    @TableField("enabled")
    @NotBlank(message ="enabled不能为空喵~")
    private String enabled;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;

    @TableField("deleted")
    @TableLogic //告诉Mybatis-plus这个是逻辑删除字段 后面的删除逻辑会变成把deleted字段值更新成1
    private String deleted;
}
