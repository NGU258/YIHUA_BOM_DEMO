package com.yihua.bom.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.yihua.bom.constants.Enum.bom.BomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("bom_header")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BomHeader {
    //在调用saveOrUpdate时由于这里指定的是IdType.AUTO 也就是主键自增
    //我如果手动指定那个要保存进去的对象id值是3 则Mybatis-Plus会自动忽略掉我设置的这个3 始终让数据库自增 设置成那个上次记录最大值id+1
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("bom_code")
    private String bomCode;

    @TableField("bom_name")
    private String bomName;

    @TableField("product_id")
    private Long productId;

    @TableField("product_code")
    private String productCode;

    @TableField("product_name")
    private String productName;

    @TableField("bom_version")
    private String bomVersion;

    @TableField("bom_type")
    private String bomType;

    @TableField("base_qty")
    private BigDecimal baseQty;//这里为了方便后面直接调用方法进行计算  不需要再进行转换(Integer -> BigDecimal)

    @TableField("unit")
    private String unit;


    @TableField("status")
    private String status;

    @TableField("is_default")
    private Integer isDefault;

    @TableField("effective_date")
    private LocalDateTime effectiveDate;

    @TableField("expire_date")
    private LocalDateTime expireDate;

    @TableField("remark")
    private String remark;

    //告诉Mybatis-Plus这个字段后面在执行插入的时候要进行自动填充
    //这里的话会被那个实现了MetaObjectHandler类的子类方法捕获到
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time" , fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;

}
