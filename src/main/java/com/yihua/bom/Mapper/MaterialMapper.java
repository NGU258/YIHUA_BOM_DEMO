package com.yihua.bom.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yihua.bom.entity.Material;

// 一般来说一个具体的类实现一个接口
// 由于接口本身没有方法体 所以接口之间是没法相互实现的
// 但接口之间是可以继承的 就像一个新规则可以继承以前的旧规则
public interface MaterialMapper extends BaseMapper<Material> {
}
