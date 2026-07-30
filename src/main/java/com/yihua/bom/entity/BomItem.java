package com.yihua.bom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Integer qty;

    @TableField("unit")
    private String unit;

    @TableField("loss_rate")
    private BigDecimal lossRate;

    @TableField("fixed_loss_qty")
    private Integer fixedLossQty;

    @TableField("issue_type")
    private String issueType;

    @TableField("process_code")
    private String processCode;

    @TableField("process_name")
    private String processName;

    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("udpate_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

}
