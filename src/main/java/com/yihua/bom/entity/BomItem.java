package com.yihua.bom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


// 投料方式是正常投料： 在生产产品前拿工单去仓库里面领
// 投料方式是倒冲 这个是系统自动识别 然后扣减
// 投料方式是手工投料 是由人工手动计算的
@TableName("bom_item")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BomItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("bom_id")
    private Integer bomId;

    @TableField("parent_id")
    private Integer parentId;

    @TableField("material_id")
    private Integer materialId;

    @TableField("material_code")
    private String materialCode;

    @TableField("material_name")
    private String materialName;

    @TableField("material_spec")
    private String materialSpec;

    @TableField("item_no")
    private Integer itemNo;

    @TableField("qty")
    private BigDecimal qty;

    @TableField("unit")
    private String unit;

    @TableField("loss_rate")
    private BigDecimal lossRate;

    @TableField("fixed_loss_qty")
    private BigDecimal fixedLossQty;

    @TableField("issue_type")
    private String issueType;

    @TableField("process_code")
    private String processCode;

    @TableField("process_name")
    private String processName;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time" , fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //这里好像会把value里面的字段名当成Mybaits-Plus生成SQL时的查询字段
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted = 0;

    //Mybatis-Plus默认行为： 会把实体类中的每个字段都映射到数据库字段中
    //也就是说下面这个字段如果不指定exists=fa lse告诉Mybatis-Plus数据库中不存在这个字段的话(不用管它)
    //Mybatis-Plus生成的Sql中会出现这个字段： select ……,child_node from XXX 这样的话就会报错 因为数据库中是没有这个字段的
    //这里赋值一个空列表 防止后面链式调用的时候出现空指针异常 属于一种防御性编程 是一种比较好的编程习惯
    //这样的话前端那里看到的就是空列表[] 而不是null了
    //由于 java7引入的 "类型自动推断机制" 后面<>里面可以不指定类型 它会根据左边的(List<BomItem>)自动推断出右边的类型的
    @TableField(exist = false)
    private List<BomItem> childNode = new ArrayList<BomItem>();
}
