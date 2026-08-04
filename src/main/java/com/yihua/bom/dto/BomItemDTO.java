package com.yihua.bom.dto;

import com.yihua.bom.constants.Enum.bom.BomIssueType;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

//测试数据
//      {
//        "parentId":4,
//        "materialId":7,
//        "itemNo":10,
//        "qty":3,
//        "unit":"组"
//       }
@Data
@Builder
public class BomItemDTO {

    //这里的话BomId没有放进来的原因是因为
    //这个BomId已经在请求路径中指明了
    //所以就不需要再在JSON里面指定了
    //这样也可以防止前端不小心指定了一个错误的BomId
    //这是一个最佳实践 DTO里面只需要放需要填的相关字段就可以了

    @Builder.Default
    private Long parentId = 0l;

    //这里有个知识点： 冗余存储
    //意思就是说把另一个表的中部分字段也存到当前表中 这样每次查询的时候就不需要再关联另一张表了
    //这样的设计性能更好  也就是空间换时间的逻辑
    //这里还有一个好处就是在更新的时候 当用户没有传对应material的相关字段时
    //在更新逻辑中防止使用BeanUtils.copyProperties函数直接转为null
    @NotNull(message = "物料id不能为空")
    private Long materialId;


    //这里保持默认的 null
    //后面的判断逻辑会根据这个null 来决定它的行号值
    //如果有值就是它自己 如果没值那就默认是0
    //如果这里直接给个默认值是0的话 用户那边也指定是0
    //所有行号记录都是的0的话就不好排序了
    private Integer itemNo ;

    @DecimalMin(value = "0.0001" , message = "标准用量(qty)必须大于0")
    private BigDecimal qty;

    private String unit;

    //不指定的话默认就是null了 防止后面的空指针异常
    //损耗率代表生产时会浪费多少
    //假如生成一个2平方米的木板 但切割时会有5%的边角料浪费掉了
    //而实际领料=2*(1+5%)=2*1.05=2.1
    //这里多出来的0.1就是损耗了 但仓库会实际按2.1发料
    //这个字段是比例损耗  生产多少损耗就会成比例增长
    @Builder.Default
    @DecimalMin(value = "0" , message = "损耗率(loss_rate)不能小于0")
    private BigDecimal lossRate = BigDecimal.ZERO;


    //这个字段是固定损耗  不管生产多少损耗都是固定的 比如开机时固定的损耗
    @Builder.Default
    private BigDecimal fixedLossQty = BigDecimal.ZERO;

    //默认正常投料
    @Builder.Default
    private String issueType = BomIssueType.NORMAL.getValue();

    private String prcessCode;

    private String processName;

    private String remark;

}
