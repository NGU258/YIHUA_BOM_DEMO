package com.yihua.bom.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yihua.bom.entity.BomHeader;
import org.apache.ibatis.annotations.Mapper;

//@Mapper注解的作用与@Service类似 都会生成一个Bean由IOC容器管控
//但这个并不是生成对应的实现类 而是会生成一个代理对象
//这个代理对象会实现当前这个接口中定义的所有方法
@Mapper
public interface BomHeaderMapper extends BaseMapper<BomHeader> {
}
