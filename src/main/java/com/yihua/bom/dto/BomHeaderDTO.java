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

//测试数据
// {
//        "bomCode":"fairyCat001",
//        "bomName":"BOM-E-001",
//        "productId":1,
//        "bomVersion":"V1",
//        "unit":"单位"
//}
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

    @Builder.Default
    //这里为了方便后面直接调用方法进行计算  不需要再进行转换(Integer -> BigDecimal)
    private BigDecimal baseQty = BigDecimal.ONE;

    private String unit;

    //前端不写的话默认就是草稿状态
    //@Builder.Default注解的作用就是会保留当前设置的默认值
    //这样就不会因为JSON反序列化时因为全参构造中的赋值逻辑 给它重新赋值为null了

    //还有一点就是它在调用.builder静态方法生成实例的时候那个实例里面的属性值默认是null(引用类型)
    //所以这里设置的默认值就被覆盖掉了 因此在属性上面加个@Builder.Default使得这里设置的默认值是生效的
    //最佳实践： 每个使用了建造者模式的类 里面如果有设置默认值的话就都在这个属性上面加@Builder.Default
    @Builder.Default
    private String status = BomStatus.DRAFT.getValue();

    //这里的话因为是一个包装类 不写的话默认值就是null  所以需要给它传一个 0 防止空指针异常
    @Builder.Default
    private Integer isDefault = 0;

    private LocalDateTime effectiveDate;

    private LocalDateTime expireDate;

    private String remark;

}
