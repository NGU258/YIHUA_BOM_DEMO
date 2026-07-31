package com.yihua.bom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yihua.bom.Mapper.MaterialMapper;
import com.yihua.bom.dto.MaterialDTO;
import com.yihua.bom.entity.Material;
import com.yihua.bom.exception.fairyCatException;
import com.yihua.bom.service.IMaterialService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

// @AllArgsConstructor 注解生成的构造器所包含的字段 ： 所有final与非final字段
// 后面如果自己再加一点其它的类变量 则也会被生成出来 但一般这里的全参构造器只生成那些需要依赖进来的对象就可以了

// @RequiredArsConstructor 注解生成的构造器所包含的字段：  只包含final字段跟@NonNUll字段
// 这里的语义就比较明确  只有需要的依赖才放到构造器里面
// @NonNull 注解被用在方法形参跟字段上面 作用是这个值不能为null 如果是null的话就会抛出对应的异常
@Service
@RequiredArgsConstructor // 用来生成全参构造  主要针对于final字段
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper,Material> implements IMaterialService {

    private final MaterialMapper materialMapper;
    @Override
    public Material createMaterial(@NonNull MaterialDTO m) {

        Material material = new Material();
        BeanUtils.copyProperties(m,material);

        int result = materialMapper.insert(material);

        if(result != 1)
            throw new fairyCatException("插入失败");

        return material;

    }
}
