package com.yihua.bom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yihua.bom.Mapper.MaterialMapper;
import com.yihua.bom.constants.Enum.bom.BomStatus;
import com.yihua.bom.constants.Enum.material.MaterialType;
import com.yihua.bom.dto.MaterialDTO;
import com.yihua.bom.entity.BomHeader;
import com.yihua.bom.entity.BomItem;
import com.yihua.bom.entity.Material;
import com.yihua.bom.exception.fairyCatException;
import com.yihua.bom.service.IBomHeaderService;
import com.yihua.bom.service.IBomItemService;
import com.yihua.bom.service.IMaterialService;
import com.yihua.bom.vo.BomTreeStructVo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
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

//这个@RequiredArgsConstructor有一个坑在里面
//如果下面的private final字段上面加了@Lazy注解的话 @RequriedArgsConstructor注解生成的构造器里面形参前面是不会自动加上这个@Lazy注解的
//所以如果需要解决循环依赖的话得自己手写一个全参构造才会生效

//因为在建数据库的时候使用的排序规则是utf8mb4_general_ci 后面的ci（case insensitive 翻译过来就是对实例不在意）就是不区分大小写的意思
//所以如果用条件构造器的eq来判断的时候 比如比较字符串ACTIVE,不管数据库中的值是acTIVE还是active 都是可以匹配到的
//面如果在建数据库的时候使用的排序规则是utf8mb4_general_cs（case sensitive 对实例敏感的）则就是区分大小写的意思
//此时如果判断的字符串是ACTIVE,而数据库里面存的是非全大写ACTIVE的话就会查找失败
//解决方案就是用apply方法拼接一个自定义sql 然后用upper将数据库字段status对应的值转成全大写 然后再与当前比较的大写ACTIVE比较就都能匹配到了
//使用{索引}占位符 索引从0开始 {0}代表第一个参数 {1}代表第二个参数 ，以此类推
//示例:  lqw.apply("upper(status) = {0}",BomStatus.ACTIVE.getValue());
@Service
//@RequiredArgsConstructor // 用来生成全参构造  主要针对于final字段
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper,Material> implements IMaterialService {

    private final MaterialMapper materialMapper;
    private final IBomHeaderService iBomHeaderService;
    private final IBomItemService iBomItemService;

    public MaterialServiceImpl(@Lazy MaterialMapper mm,@Lazy IBomHeaderService ib,@Lazy IBomItemService ibi){
            materialMapper = mm ;
            iBomHeaderService = ib ;
            iBomItemService = ibi ;
    }


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

    @Override
    public BomTreeStructVo BomTreeStructByMaterialId(Long materialId) {

        LambdaQueryWrapper<BomHeader> lqw_header = new LambdaQueryWrapper<>();
        lqw_header.eq(BomHeader::getProductId,materialId)

                .eq(BomHeader::getStatus,BomStatus.ACTIVE.getValue());

        //先找这个物料对应的启用Bom-Header记录
        BomHeader bomHeader =iBomHeaderService.getOne(lqw_header);
        if(Objects.isNull(bomHeader))
            throw new fairyCatException("500","该物料不存在或它在数据库中没有对应启用的BOM");

        //这里存的就是根节点的值
        BomTreeStructVo bomTreeStructVo = BomTreeStructVo.builder()
                .qty(bomHeader.getBaseQty())
                .unit(bomHeader.getUnit())
                .materialId(bomHeader.getProductId())
                .materialCode(bomHeader.getProductCode())
                .materialName(bomHeader.getProductName())
                .build();

        //这里就是存的所有子节点的值
        List<BomTreeStructVo> bomTreeStructVoList = new ArrayList<>();

        //接着拿着这条记录的bom_id去子表Bom-Item中找树中第二层的所有节点
        //这里测试后发现需要分情况 一种情况是成品(根节点) 这里可以用bomId跟ParentId去找
        //但如果是半成品(父物料节点) 就不能用bomId跟ParentId去找了 因为BomId是记录着成品物料的id  并不是半成品物料的id 直接调用findXXX传bomItem的id就可以了
        LambdaQueryWrapper<BomItem> lqw_item = new LambdaQueryWrapper<>();
        //来到这里说明肯定能查到一个物料记录 所以可以不用判断了
        Material material = getById(materialId);
        //这里测试的时候发现不区分大小写 传product跟PRODUCT都可以
        if(MaterialType.PRODUCT.getValue().equalsIgnoreCase(material.getMaterialType())){
            //如果是成品的情况
            lqw_item.eq(BomItem::getBomId,bomHeader.getId())
                    .eq(BomItem::getParentId,0);
            List<BomItem> twoNode = iBomItemService.list(lqw_item);

            //遍历第二层的所有节点 然后调用findXXX方法去查询到这些节点对应的BOM
            for(BomItem cur: twoNode)
                bomTreeStructVoList.add(findBomTreeStructByBomItemId(bomHeader.getId(),cur.getId()));
        }else{
            //半成品或原材料的情况  老师说没有半成品这种情况 只需要传成品的就可以了
            //郑经理说公司里面的成品或半成品一般都需要看它的BOM
            //所以这里的设计就是这个半成品的BOM不管在哪颗树下 比如主板不管是在笔记本电脑上还是台式机电脑上 在查看主板BOM结构时默认都是一样的
            lqw_item.eq(BomItem::getMaterialId,materialId);
            BomItem  bomItem = iBomItemService.getOne(lqw_item);

            //如果是半成品 直接返回它的BOM结构就可以了
            return findBomTreeStructByBomItemId(bomItem.getBomId(),bomItem.getId());
        }

        bomTreeStructVo.setChildNode(bomTreeStructVoList);

        return bomTreeStructVo;

    }

    @Override
    public BomTreeStructVo findBomTreeStructByBomItemId(Long bomId,Long bomItemId) {

        BomItem bomItem = iBomItemService.getById(bomItemId);
        if(Objects.isNull(bomItem))
            throw new fairyCatException("500","在递归查询BOM树时发现有个Bom明细id在数据库中不存在");

        BomTreeStructVo bomTreeStructVo = BomTreeStructVo.builder()
                .qty(bomItem.getQty())
                .unit(bomItem.getUnit())
                .materialId(bomItem.getMaterialId())
                .materialCode(bomItem.getMaterialCode())
                .materialName(bomItem.getMaterialName())
                .build();

        LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomItem::getBomId,bomId)
            .eq(BomItem::getParentId,bomItemId);

        //然后再找当前节点的所有子节点
        List<BomItem> childNode = iBomItemService.list(lqw);
        for(BomItem cur: childNode){
                List<BomTreeStructVo> bomTreeStructVoList = new ArrayList<>();
                bomTreeStructVoList.add(findBomTreeStructByBomItemId(bomId,cur.getId()));
                bomTreeStructVo.setChildNode(bomTreeStructVoList);
        }

        return bomTreeStructVo;
    }
}
