package com.yihua.bom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

// @AllArgsConstructor 注解生成的构造器所包含的字段 ： 所有final与非final字段
// 后面如果自己再加一点其它的类变量 则也会被生成出来 但一般这里的全参构造器只生成那些需要依赖进来的对象就可以了

// @RequiredArsConstructor 注解生成的构造器所包含的字段：  只包含final字段跟@NonNUll字段
// 这里的语义就比较明确  只有需要的依赖才放到构造器里面
// @NonNull 注解是在lombok依赖中的
// Non是拉丁语前缀 而Not是英语单词 虽然两者表现形式不一样 但它们两者都表达同一个意思 都是否定的意思
// @NonNull 注解被用在方法形参跟字段上面 作用是这个值不能为null 如果是null的话就会抛出对应的异常
// 比如在方法形参名a前面加了@NonNull 则lombok在编译时会在方法体中添加这个if判断 即if(a == null) 就throw出对应的异常
// 它与NotNull的区别是 NotNull是Validation依赖下的校验注解  需要结合@Valid注解或@Validated注解才能生效
// 如果方法接收的DTO对象前面没有加上@Valid或@Validated注解的话 前面设置的这些@NotNull校验注解就不会生效.
@Service
@RequiredArgsConstructor // 用来生成全参构造  主要针对于final字段
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper,Material> implements IMaterialService {

    private final MaterialMapper materialMapper;

    // 事务的话就是要么都成功 要么都失败 比如我发红包100给张三 我的余额扣了100  但张三的余额如果没被加100是不被允许的 这个就是事务
    // 所以@Transactional 这个注解的作用就是保证事务的一致性 如果发生了错误会及时回滚 让数据回到操作之前的状态
    // 换一种说法就是 它会告诉spring当前这个方法对数据库的所有操作都要放在同一个事务里面
    // 最佳实践： 写操作都需要用加这个@Transactional注解来保证事务的一致性
    // 但读操作就不需要了 因为读的话只要不是并发场景 读到的数据都是一样的 这样还可以减少性能开销
    // 这里不指定事务注解里面的rollbackFor属性的话默认是只回滚运行时异常和错误  但非运行时异常是不会被回滚的
    // 如果想在触发非运行时异常就回滚的话需要指定 rollbackFor = Exception.class
    @Override
    @Transactional
    public Material createMaterial(MaterialDTO m) {

        Material material = new Material();
        BeanUtils.copyProperties(m,material);

        material.setDeleted(0);
        int result = materialMapper.insert(material);

        if(result != 1)
            throw new fairyCatException("插入失败");

        return material;

    }

    @Override
    public IPage<Material> listMaterial(Long pageNum, Long count, String key) {

        //创建分页对象
        Page<Material> page = new Page<>(pageNum,count);

        //创建查询条件
        LambdaQueryWrapper<Material> lqw = new LambdaQueryWrapper<>();

        //可以查询的关键字 ： 物料编码、物料名称
        //hasText函数的作用主要是判断是不是null、是不是空字符串、是不是纯空格字符串
        //字面意思就是如果有文本的话就返回true 反之如果是上面这些的话就返回false
        //判断逻辑： 如果是满足以上条件  则默认是查询所有数据 不进行分页
        //如果用户已经输入了关键字  则进行模糊查询
        if(StringUtils.hasText(key))
             lqw.like(Material::getMaterialCode,key)
                 .or()
                 .like(Material::getMaterialName,key);

        //将查询到的结果降序一下 把最新创建的数据放到开头
        //这里只需要写一个参数是因为 方法名XXEesc已经指定了对应的排序方式
        //Material::getCreateTime 这里是一个方法引用  里面的执行逻辑会把这个CreateTime截取出来  然后转换成下划线的形式
        //这样就可以知道对应的数据库字段了  所以只需要传一个参数就可以了
        lqw.orderByDesc(Material::getCreateTime);

        //最后再执行实际的分页操作进行分页
        return baseMapper.selectPage(page,lqw);

    }

    @Override
    public Material getMaterial(Long materialId) {
        Material material = getById(materialId);

        //这样写的话语义更加明确
        if(Objects.isNull(material))
                throw new fairyCatException("500","未查询到该物料信息");

        return material;
    }

    @Override
    @Transactional
    public Material updateMaterial(Long materialId, MaterialDTO mDto) {
         Material material = getById(materialId);
         if(Objects.isNull(material))
                throw new fairyCatException("500","在尝试更新时未查询到该物料信息");
         BeanUtils.copyProperties(mDto,material);
         Boolean result = saveOrUpdate(material);
         if(!result)
             throw new fairyCatException("500","更新失败");
         return material;
    }

    @Override
    @Transactional
    public Material deleteMaterialById(Long materialId) {
        //逻辑： 返回的时候给用户显示已删除的那个物料信息
        Material material = getById(materialId);
        //这里如果用户重复执行删除操作的话提示相关的信息
        if(Objects.isNull(material))
             throw new fairyCatException("500","当前要删除的物料在数据库中并不存在");

        Integer result = baseMapper.deleteById(materialId);
        if(result == 0)
                throw new fairyCatException("500","删除失败");

        return material;
    }
}
