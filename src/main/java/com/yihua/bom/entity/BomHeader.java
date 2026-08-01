package com.yihua.bom.entity;

import com.baomidou.mybatisplus.annotation.*;
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
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("bom_code")
    private String bomCode;

    @TableField("bom_name")
    private String bomName;

    @TableField("product_id")
    private Integer productId;

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
    private Integer deleted = 0;

}
