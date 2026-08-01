package com.yihua.bom.dto;


import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.yihua.bom.constants.Enum.bom.BomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomHeaderDTO {

    private String bomCode;

    private String bomName;

    //这里的话因为已经知道了物料ID了 后端可以通过这个ID找到对应的产品Code跟产品Name
    //所以的话后面就可以不用在这个DTO对象中再添加productCode跟productName这两个字段了
    //这样的话可以避免一个风险： 万一前端造假
    private Long productId;

    private String bomVersion;

    private String bomType;

    //这里为了方便后面直接调用方法进行计算  不需要再进行转换(Integer -> BigDecimal)
    private BigDecimal baseQty = BigDecimal.ONE;

    private String unit;

    //前端不写的话默认就是草稿状态
    //@Builder.Default注解的作用就是会保留当前设置的默认值
    //这样就不会因为JSON反序列化时因为全参构造中的赋值逻辑 给它重新赋值为null了
    @Builder.Default
    private String status = BomStatus.DRAFT.getValue();

    //这里的话因为是一个包装类 不写的话默认值就是null  所以需要给它传一个 0 防止空指针异常
    private Integer isDefault = 0;

    private LocalDateTime effectiveDate;

    private LocalDateTime expireDate;

    private String remark;

    private Integer deleted;

}
