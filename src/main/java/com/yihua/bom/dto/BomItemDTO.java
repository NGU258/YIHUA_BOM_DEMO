package com.yihua.bom.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BomItemDTO {

    //这里的话BomId没有放进来的原因是因为
    //这个BomId已经在请求路径中指明了
    //所以就不需要再在JSON里面指定了
    //这样也可以防止前端不小心指定了一个错误的BomId
    //这是一个最佳实践 DTO里面只需要放需要填的相关字段就可以了

    private Long parentId;

    private Long materialId;

    //这里保持默认的 null
    //后面的判断逻辑会根据这个null 来决定它的行号值
    //如果有值就是它自己 如果没值那就默认是0
    //如果这里直接给个默认值是0的话 用户那边也指定是0
    //所有行号记录都是的0的话就不好排序了
    private Integer itemNo ;

    private BigDecimal qty;

    private String unit;

    //不指定的话默认就是null了 防止后面的空指针异常
    private BigDecimal lossRate = BigDecimal.ZERO;

    private BigDecimal fixedLossQty = BigDecimal.ZERO;

    private String issueType;

    private String prcessCode;

    private String processName;

    private String remark;

    private Integer deleted;
}
