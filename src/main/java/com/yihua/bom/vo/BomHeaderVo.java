package com.yihua.bom.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//Dto(数据传输对象)是负责接收用户传过来的JSON数据 也就是把数据带进来 然后在业务层进行相应的处理
//VO(视图对象)是负责将处理好的数据返回给前端进行渲染
//总结来说DTO负责进 VO负责出
//实体类主要是用来跟数据库进行映射的 所以这里一般不需要加啥默认值跟校验(特殊情况除外,根据实际需求来)
//校验主要是在DTO对象中的 因为DTO是接收用户传过来的数据的 同理也不需要给VO加啥校验和默认值

//核心设计思路
//dto中只放需要用户填的字段 而其它的一些字段隐藏掉不让前端改 这样的话安全性更强
//vo中只放需要展示给前端看的字段

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BomHeaderVo {
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String status;
    private Integer isDefault;
    private String bomCode;
    private String bomName;
    private Long id;
}
