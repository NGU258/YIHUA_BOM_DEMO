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
import com.yihua.bom.vo.MaterialVo;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

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

        //用于错误拼接
        StringBuilder sb = new StringBuilder();

        Material material = new Material();
        BeanUtils.copyProperties(m,material);

        material.setDeleted(0);

        LambdaQueryWrapper<Material> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Material::getMaterialCode,m.getMaterialCode());

        if(!Objects.isNull(getOne(lqw)))
             sb.append("物料编码不能重复");

        if(sb.length() != 0)
             throw new fairyCatException("500",sb.toString());

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

        LambdaQueryWrapper<BomHeader> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(BomHeader::getProductId,materialId);
        BomHeader bomHeader = iBomHeaderService.getOne(lqw1);
        if(!Objects.isNull(bomHeader))
            throw new fairyCatException("400","该物料已经被BOM主表引用了，无法删除");

        LambdaQueryWrapper<BomItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(BomItem::getMaterialId,materialId);
        BomItem bomItem = iBomItemService.getOne(lqw2);
        if(!Objects.isNull(bomItem))
            throw new fairyCatException("400","该物料已经被BOM子表引用了，无法删除");

        Integer result = baseMapper.deleteById(materialId);
        if(result == 0)
                throw new fairyCatException("500","删除失败");

        return material;
    }

    @Override
    public BomTreeStructVo BomTreeStructByMaterialId(Long materialId) {

        //先定义一个存储BOM树结构的对象
        BomTreeStructVo bomTreeStructVo = new BomTreeStructVo();

        //先判断用户传入的这个物料存不存在
        LambdaQueryWrapper<Material> lqw =new LambdaQueryWrapper<>();
        lqw.eq(Material::getId,materialId);
        Material material = getOne(lqw);
        if(Objects.isNull(material))
            throw new fairyCatException("400","物料表中不存在该物料，请填入一个正确的物料id");

        //如果用户传入的是原材料 则提示原材料没有对应的BOM
        if(MaterialType.RAW_MATERIAL.getValue().equalsIgnoreCase(material.getMaterialType()))
             throw new fairyCatException("400","原材料没有对应的BOM结构");

        //接着就可以去找这个物料对应已启用的BOM了 找到了说明可以展开
        //优化思路 先把物料对应的所有BOM全部都查出来 然后再用for循环遍历找那个ACTIVE状态的BOM 这样就可以不用两次请求都打到数据库了
        LambdaQueryWrapper<BomHeader> lqw_header = new LambdaQueryWrapper<>();
        lqw_header.eq(BomHeader::getProductId,materialId); //通过它的物料id找到所有的BOM
        List<BomHeader> bomHeaderList =iBomHeaderService.list(lqw_header);
        if(Objects.isNull(bomHeaderList))
            throw new fairyCatException("400","该物料没有对应的BOM结构，请联系管理员添加");

        //在遍历之前先创建一个临时Bom 用于当没查找ACTIVE的BOM时在后面填充根节点信息返回给用户展示
        BomHeader bomHeader = null;
        //遍历物料对应的所有BOM
        for(BomHeader cur: bomHeaderList){
            //找ACTIVE的BOM  也就说明这个节点可以展开
            if(BomStatus.ACTIVE.getValue().equalsIgnoreCase(cur.getStatus())){

                //接着遍历树中第二层的所有子节点 分两种情况 原材料的情况前面已经判断过了
                //情况一： 传入的物料id是成品(树的根节点) 可以用bomId跟ParentId去找它树下的所有子节点
                //情况二： 传入的物料id是半成品(物料节点的父节点) 就不能用bomId跟ParentId去找了
                //        因为BomId的设计主要是存成品(树的根节点)的id  而不是半成品物料的id
                //        而对于半成品物料 通过调用自己写的递归方法findBomTreeStructByBomItemId传入半成品的bomItemid返回它的BOM结构就行了

                //这里后面的两个分支都需要用到这个条件构造器 就写到前面了
                LambdaQueryWrapper<BomItem> lqw_item = new LambdaQueryWrapper<>();

                //首先判断一下当前节点的物料是成品还是半成品
                //注意equals是严格区分大小写的 但我需要不区分大小写 所以调用的是equalsIgnoreCase方法 这样传大小写的物料类型就都可以
                if(MaterialType.PRODUCT.getValue().equalsIgnoreCase(material.getMaterialType())){
                    //成品的逻辑 先存下根节点的值
                    bomTreeStructVo = BomTreeStructVo.builder()
                            .qty(cur.getBaseQty())
                            .unit(cur.getUnit())
                            .materialId(cur.getProductId())
                            .materialCode(cur.getProductCode())
                            .materialName(cur.getProductName())
                            .id(cur.getId())
                            .build();

                    //接着就开始存所有子节点的值
                    //先找树的第二层节点 对应的parentId就是0
                    List<BomTreeStructVo> bomTreeStructVoList = new ArrayList<>();
                    lqw_item.eq(BomItem::getBomId,cur.getId())
                            .eq(BomItem::getParentId, 0L);
                    List<BomItem> twoNode = iBomItemService.list(lqw_item);
                    if(Objects.isNull(twoNode)) //如果没有子节点
                         return bomTreeStructVo; //直接把根节点信息返回给用户看就可以了

                    //如果有就遍历第二层的所有节点 这里也有两种情况
                    //一种是半成品
                    //另一种就是原材料
                    //这里不会是成品 因为成品是在根节点的位置 不会出现自己存自己的情况
                    //这里最关键的就是调用递归方法findBomTreeStructByBomItemId方法来查询它们的BOM结构
                    for(BomItem two: twoNode){

                        //接着判断当前这个子节点的物料类型
                        //检测： 先去物料表里面找一下存不存在
                        LambdaQueryWrapper<Material> lqw_m = new LambdaQueryWrapper<>();
                        lqw_m.eq(Material::getId,two.getMaterialId());
                        Material materialRaw = getOne(lqw_m);
                        if(Objects.isNull(materialRaw))
                            throw new fairyCatException("500","展开BOM树时发现异常： 有个节点的物料在物料表中并没有记录，请联系管理员");

                        //接着才是判断类型
                        //如果是原材料 直接加进来就可以了  需要累乘qty
                        if(MaterialType.RAW_MATERIAL.getValue().equalsIgnoreCase(materialRaw.getMaterialType())){
                            bomTreeStructVoList.add(BomTreeStructVo.builder()
                                    .qty(two.getQty().multiply(cur.getBaseQty())) //第二层原材料的qty * 父节点的qty
                                    .unit(two.getUnit())
                                    .materialName(two.getMaterialName())
                                    .materialCode(two.getMaterialCode())
                                    .materialId(two.getMaterialId())
                                    .build());
                        }else {
                            //如果是半成品 需要看它的BOM状态是否展开(ACTIVE)
                            //隐藏的知识点  一个物料会对应多个不同BOM状态的记录 如果我需要找启用状态的那条
                            //可以看is_default值是1的情况 也可以看status是ACTIVE的情况 它们两是一对的 另一种说法它们就像是事务一致性

                            lqw_header.clear();
                            //这里也是用的之前的优化思路 先找出这个物料所有的BOM
                            lqw_header.eq(BomHeader::getProductId,two.getMaterialId());
                            List<BomHeader> bhList = iBomHeaderService.list(lqw_header);
                            if(Objects.isNull(bhList))
                                throw new fairyCatException("400","在展开BOM树时发现有个半成品物料[id: "+two.getMaterialId()+"]没对应的BOM结构，请联系管理员添加");

                            boolean signC = true; //防止第一遍没找到 但第二遍找到了  不好用bh1是否等于null来判断 所以需要加这个开关
                            //然后再全部遍历一遍 找启用状态的
                            for(BomHeader bh: bhList){
                                if(BomStatus.ACTIVE.getValue().equalsIgnoreCase(bh.getStatus())){
                                    //启用状态的话就说明可以展开 调用递归方法继续递归进去找到的BOM结构
                                    //下面这个递归方法刚开始的时候里面的逻辑会把它自己找出来 然后累乘的刚刚传入的这个值  这里只需要传根节点的qty就可以了
                                    bomTreeStructVoList.add(findBomTreeStructByBomItemId(cur.getId(), two.getId(),cur.getBaseQty()));
                                    signC =  false;
                                    break;
                                }
                            }
                            if(signC){
                                //来到这说明这个半成品没有可展开的BOM
                                //存入这个根节点的信息返回给用户看就可以了
                                bomTreeStructVoList.add(
                                        BomTreeStructVo.builder()
                                                .qty(two.getQty().multiply(cur.getBaseQty()))
                                                .unit(two.getUnit())
                                                .materialId(two.getMaterialId())
                                                .materialName(two.getMaterialName())
                                                .materialCode(two.getMaterialCode())
                                                .id(two.getId())
                                                .build()
                                );
                            }

                        } //第二层子节点是半成品的情况->结束
                    }//遍历第二层子节点->结束

                    //然后将第二层子节点遍历的结果放到这个视图对象的数组中保存
                    bomTreeStructVo.setChildNode(bomTreeStructVoList);

                    //最后返回就可以了
                    return bomTreeStructVo;

                }//成品逻辑结束
                else {
                    //半成品逻辑 用户传入的物料id是半成品
                    //这里的设计就是这个半成品的BOM不管在哪颗树下 比如主板不管是在笔记本电脑上还是台式机电脑上 在查看主板BOM结构时默认都是一样的

                    //1. 这里就是半成品启用状态的BOM： 主表对象cur

                    //2. 去找这个半成品在BomItem表中的BomItemId 随便找一个就可以了 因为它路径是没有传bomId的 所以我就默认结构都是一样的了
                    //这里拿它的物料id来查
                    LambdaQueryWrapper<BomItem> bomItemLQW = new LambdaQueryWrapper<>();
                    bomItemLQW.eq(BomItem::getMaterialId,cur.getProductId());
                    List<BomItem> bomItemList = iBomItemService.list(bomItemLQW);
                    if(Objects.isNull(bomItemList)) {
                        //如果是空的，说明这个半成品的展开状态下 它是没有子节点的 直接返回根节点信息就可以了
                        return BomTreeStructVo.builder()
                                .qty(cur.getBaseQty()) //这里用户传入的半成品没有展开 所以只需要展示根节点信息就行了 而不需要累乘
                                .unit(cur.getUnit())
                                .materialName(cur.getProductName())
                                .materialCode(cur.getProductCode())
                                .materialId(cur.getProductId())
                                .id(cur.getId())
                                .build();
                    }

                    //如果这个半成品在不同树下出现 拿第一次出现的节点信息
                    //所以默认用户查的这个半成品的BOM 都是同一棵树
                    BomItem bomItem = bomItemList.get(0);

                    //3. 递归进去查BOM结构就可以了
                    return findBomTreeStructByBomItemId(bomItem.getBomId(),bomItem.getId(),BigDecimal.ONE);
                }//半成品逻辑结束
            }//if条件结束

            //如果没找到就保存当前这个非ACTIVE状态的BOM信息
            bomHeader = cur;

        }//遍历当前物料所有BOM结束

        //来到这里就说明这个物料没有展开状态的BOM
        //这里我有三种选择 一种是直接返回null(表示没有展开，需要展开才能看到)
        //另一种情况就是抛出异常 但感觉不是很推荐
        //最后一种就是我把它的根节点信息展示出来  但子节点不展示 显示为[]就可以了

        //所以直接返回它的根节点信息就可以了
        return BomTreeStructVo.builder()
                .qty(bomHeader.getBaseQty())
                .unit(bomHeader.getUnit())
                .materialName(bomHeader.getProductName())
                .materialCode(bomHeader.getProductCode())
                .materialId(bomHeader.getProductId())
                .id(bomHeader.getId())
                .build();
    }


    //bomId主要是判断在哪颗树下 这个方法主要还是查半成品的BOM结构 所以需要传入它的bomItemId
    //multQty主要用于累乘计算 如果传进来的是1 则说明这个半成品就是根节点 否则就说明它是树的子结点 传入的是累乘下来的值
    @Override
    public BomTreeStructVo findBomTreeStructByBomItemId(Long bomId,Long bomItemId,BigDecimal multQty) {

        //1. 先找到这个bomId树下物料所在的记录(这里可能递归到半成品 也可能递归到原材料) 用于保存根节点信息
        LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomItem::getBomId,bomId)
                .eq(BomItem::getId,bomItemId);
        BomItem bomItem = iBomItemService.getOne(lqw); //刚开始传进来的是半成品的节点信息
        if(Objects.isNull(bomItem))
            throw new fairyCatException("500","在递归查询"+bomId+"树时发现有个BomItemId在数据库中不存在,请联系管理员");

        multQty = bomItem.getQty().multiply( multQty); //累乘半成品父节点的qty值
        BomTreeStructVo bomTreeStructVo = BomTreeStructVo.builder()
                .qty(multQty)
                .unit(bomItem.getUnit())
                .materialId(bomItem.getMaterialId())
                .materialCode(bomItem.getMaterialCode())
                .materialName(bomItem.getMaterialName())
                //根据物料id获取到它的头节点id
                .id(iBomHeaderService.getActiveBomHeaderIdByMaterialId(bomItem.getMaterialId()))
                .build();


        lqw.clear();
        //2. 然后找这个半成品下的所有子节点
        //递归结束条件一： 如果它是叶子节点的原材料 则没有谁的parentId会存它 下面的这个list就会判空 后面的for循环就不会执行 子节点的位置存个[]进去就可以了
        //子节点的parentId会存放着它的id
        lqw.eq(BomItem::getBomId,bomId)
            .eq(BomItem::getParentId,bomItemId);
        List<BomItem> childNode = iBomItemService.list(lqw);
        if(Objects.isNull(childNode)) //如果没有子节点 直接返回它前面的根节点信息就可以了
            return bomTreeStructVo;

        //定义一个列表来存它的子节点BOM结构
        List<BomTreeStructVo> bomTreeStructVoList = new ArrayList<>();
        //3. 遍历半成品的子节点
        for(BomItem cur: childNode){

            //3.1 先判断当前这个子节点对应的物料在物料表中是否存在
            LambdaQueryWrapper<Material> lqw_material = new LambdaQueryWrapper<>();
            lqw_material.eq(Material::getId,cur.getMaterialId());
            Material  material = getOne(lqw_material);
            if(Objects.isNull(material))
                throw new fairyCatException("500","在展开树BOM结构时检测到有个节点的物料在物料表中不存在，请联系管理员(逻辑findBomTreeStructByBomItemId中)");

            //3.2 判断这个子节点物料的类型
            //3.2.1 如果这个物料是原材料 就直接加进去
            if(MaterialType.RAW_MATERIAL.getValue().equalsIgnoreCase(material.getMaterialType())){
                bomTreeStructVoList.add(BomTreeStructVo.builder()
                                        .qty(cur.getQty().multiply(multQty)) //当前子节点*它父节点的qty
                                        .unit(cur.getUnit())
                                        .materialName(cur.getMaterialName())
                                        .materialCode(cur.getMaterialCode())
                                        .materialId(cur.getMaterialId())
                                        .build());
            }else{
                //3.2.2 这个节点是半成品
                //再判断一下它有没有展开状态
                //优化思路： 先全部查一遍这个物料所有状态的BOM 然后再遍历找ACTIVE的BOM(展开状态)
                LambdaQueryWrapper<BomHeader> lqw_all = new LambdaQueryWrapper<>();
                lqw_all.eq(BomHeader::getProductId, cur.getMaterialId());
                List<BomHeader> bomHeaderList = iBomHeaderService.list(lqw_all);
                if (Objects.isNull(bomHeaderList))
                    throw new fairyCatException("500", "检测到有个半成品物料["+cur.getMaterialId()+"]的BOM信息不存在，请联系管理员添加");

                boolean sign = true;
                //遍历所有的BOM状态
                for(BomHeader curH: bomHeaderList){
                    //找到它启用状态的BOM
                    if(BomStatus.ACTIVE.getValue().equalsIgnoreCase(curH.getStatus())){
                        //如果有一个启用状态的BOM(这里的bomItemId是已知的 就是前面的cur.getId)
                        //直接递归进去继续找BOM结构就行了
                        bomTreeStructVoList.add(findBomTreeStructByBomItemId(bomId, cur.getId(),multQty));
                        sign = false;
                        break;
                    }
                }
                if(sign){
                    //如果没找到一个启用状态的BOM 就把根节点展示给用户看
                    bomTreeStructVoList.add(BomTreeStructVo.builder()
                            .qty(cur.getQty().multiply(multQty)) //如果没有展开 就把之前累乘的结果乘进来
                            .unit(cur.getUnit())
                            .materialId(cur.getMaterialId())
                            .materialCode(cur.getMaterialCode())
                            .materialName(cur.getMaterialName())
                            .id(iBomHeaderService.getActiveBomHeaderIdByMaterialId(cur.getMaterialId()))
                            .build());
                }
            }//半成品 逻辑结束
        } //子节点遍历 逻辑结束

        //之后把结果存进来返回就可以了
        bomTreeStructVo.setChildNode(bomTreeStructVoList);
        return bomTreeStructVo;
    }

    @Override
    public List<MaterialVo> summaryToTalQtyByMaterialId(Long materialId) {

        List<MaterialVo> materialVoList = new ArrayList<>();

        //判断传入的物料id是原材料的情况
        LambdaQueryWrapper<Material> lqw_material = new LambdaQueryWrapper<>();
        lqw_material.eq(Material::getId, materialId);
        Material material = getOne(lqw_material);
        if (Objects.isNull(material))
            throw new fairyCatException("500", "填入的物料id有误，物料表中不存在该物料");

        LambdaQueryWrapper<BomHeader> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomHeader::getProductId, material.getId())
                .eq(BomHeader::getStatus, BomStatus.ACTIVE.getValue());
        //这里因为可能会有启用、停用、草稿状态的对应BOM 所以需要先看启用的 再看草稿的
        BomHeader b1 = iBomHeaderService.getOne(lqw);
        BomHeader b2 = null;
        BomHeader b3 = null;
        if (Objects.isNull(b1)) {
            //如果启用的没有找到 就找草稿状态的
            lqw.clear();
            lqw.eq(BomHeader::getProductId, material.getId())
                    .eq(BomHeader::getStatus, BomStatus.DRAFT.getValue());
            b2 = iBomHeaderService.getOne(lqw);
            if (Objects.isNull(b2)) {
                lqw.clear();
                lqw.eq(BomHeader::getProductId, material.getId());
                List<BomHeader> bomHeaderList = iBomHeaderService.list(lqw);
                if (Objects.isNull(bomHeaderList))
                    throw new fairyCatException("500", "这个物料没有对应的BOM，请去添加它的BOM结构");

                b3 = bomHeaderList.get(0);
            }
        }

        BomHeader bResult = b1 != null ? b1 : b2 != null ? b2 : b3 != null ? b3 : null;
        //这里需要用全局的 不然局部的话重新进去的时候之前存好的结果就又是空的了
        Map<Long,MaterialVo> hashMap = new HashMap<>();
        //这里判断的是第一层节点（根节点）的物料类型
        if (MaterialType.RAW_MATERIAL.getValue().equalsIgnoreCase(material.getMaterialType())) {
            //如果它是原材料的话 就直接返回它自己就可以了
            materialVoList.add(MaterialVo.builder()
                    .totalQty(bResult.getBaseQty())
                    .unit(bResult.getUnit())
                    .materialName(bResult.getProductName())
                    .materialCode(bResult.getProductCode())
                    .materialId(bResult.getProductId())
                    .build());
        }else if(MaterialType.PRODUCT.getValue().equalsIgnoreCase(material.getMaterialType())) {
            //传入的物料是成品
            //先找一下第二层的节点
            LambdaQueryWrapper<BomItem> lqw_bomItem = new LambdaQueryWrapper<>();
            lqw_bomItem.eq(BomItem::getBomId, bResult.getId()) //半成品不能这样查
                    .eq(BomItem::getParentId, 0l);
            List<BomItem> bomItemList = iBomItemService.list(lqw_bomItem);
            for (BomItem cur : bomItemList) {
                    if (isRawMaterialByMaterialId(cur.getMaterialId())) {
                        //子节点是原材料的情况
                        materialVoList.add(
                                MaterialVo.builder()
                                        .totalQty(cur.getQty().multiply(bResult.getBaseQty()))
                                        .unit(cur.getUnit())
                                        .materialName(cur.getMaterialName())
                                        .materialCode(cur.getMaterialCode())
                                        .materialId(cur.getMaterialId())
                                        .build()
                        );
                    } else {
                        //子节点是半成品或成品(这个成品是其它树下的半成品)的情况
                        hashMap = summaryMaterialTotalQty(bResult.getId(), cur.getId(), hashMap, cur.getQty().multiply(bResult.getBaseQty()));
                    }
            }
        }else{
            //传入的物料是个半成品 半成品的话就一视同仁  它们的BOM结构默认一样
            LambdaQueryWrapper<BomItem> lqw3 = new LambdaQueryWrapper<>();
            lqw3.eq(BomItem::getMaterialId,materialId);
            List<BomItem> bomItemList = iBomItemService.list(lqw3);
            if(Objects.isNull(bomItemList))
                throw new fairyCatException("400","该半成品没有对应的BOM明细");
            BomItem bomItem = bomItemList.get(0);

            //这里只需要传数量1了 因为只需要看这个半成品需要多少原材料数量
            hashMap = summaryMaterialTotalQty(bomItem.getBomId(),bomItem.getId(),hashMap,BigDecimal.ONE);
        }

        //这里存放的操作需要放这里 不然的话前面加了个炸弹 后面又重复加炸弹进来了 逻辑就不对了
        hashMap.forEach((key,value)->{
            materialVoList.add(value);
//            System.out.println("key: "+key+" value: "+value);
        });
        return materialVoList;
    }

    @Override
    //前面两个id用于找子节点
    // 这个数组List<MaterialVo> 会存储相同的物料 所以被优化成Map了
    //long存对应的物料Id，后面那个就存原材料的值
    public Map<Long,MaterialVo> summaryMaterialTotalQty(Long bomId, Long bomItemId,  Map<Long,MaterialVo> materialVoMap, BigDecimal multQty) {

        //先找一下它的子节点
        LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomItem::getBomId,bomId)
                .eq(BomItem::getParentId,bomItemId);
        List<BomItem> bomItemList = iBomItemService.list(lqw);

        if(Objects.isNull(bomItemList)){
            //找不到说明已经到原材料了

            //清空条件找一下这个原材料在bomItem中的位置
            lqw.clear();
            lqw.eq(BomItem::getBomId,bomId)
                            .eq(BomItem::getId,bomItemId);
            BomItem bomItem = iBomItemService.getOne(lqw);
            if(Objects.isNull(bomItem))
                 throw new fairyCatException("500","灵异事件，请联系程序员");

            if(Objects.isNull(materialVoMap.get(bomItem.getMaterialId()))){
                //如果这个原材料是第一次出现的话 就存进来
                materialVoMap.put(bomItem.getMaterialId(),
                         MaterialVo.builder()
                        .totalQty(multQty.multiply(bomItem.getQty()))
                        .unit(bomItem.getUnit())
                        .materialName(bomItem.getMaterialName())
                        .materialCode(bomItem.getMaterialCode())
                        .materialId(bomItem.getMaterialId())
                        .build());
            }else{
                //否则的话说明前面已经存过这个原材料了
                //就将这个原材料的qty加进来就可以了
                MaterialVo materialVo = materialVoMap.get(bomItem.getMaterialId());
                materialVo.setTotalQty(materialVo.getTotalQty().add(bomItem.getQty().multiply(multQty)));
                materialVoMap.put(materialVo.getMaterialId(),materialVo);
            }
        }

        //遍历每个子节点
        for(BomItem cur : bomItemList){
            if(isRawMaterialByMaterialId(cur.getMaterialId())){

                //在展开的时候发现是原材料
                //如果是空的话就加进去
                if(Objects.isNull(materialVoMap.get(cur.getMaterialId())))
                    materialVoMap.put(cur.getMaterialId(),
                                 MaterialVo.builder()
                                .totalQty(cur.getQty().multiply(multQty))
                                .unit(cur.getUnit())
                                .materialName(cur.getMaterialName())
                                .materialCode(cur.getMaterialCode())
                                .materialId(cur.getMaterialId())
                                .build());
                //反之就累加qty
                else{
                    MaterialVo materialVo = materialVoMap.get(cur.getMaterialId());
                    materialVo.setTotalQty(materialVo.getTotalQty().add(cur.getQty().multiply(multQty)));
                    materialVoMap.put(cur.getMaterialId(),materialVo);
                }
            }else{
                //半成品就继续递归
                return summaryMaterialTotalQty(cur.getBomId(),cur.getId(),materialVoMap,multQty.multiply(cur.getQty()));
            }
        }

        return materialVoMap;

    }

    @Override
    public Boolean isRawMaterialByMaterialId(Long materialId) {
        Material material = getById(materialId);
        if(Objects.isNull(material))
            throw new fairyCatException("400","传入的物料id有误，该物料在物料表中不存在");
        if(MaterialType.RAW_MATERIAL.getValue().equalsIgnoreCase(material.getMaterialType()))
            return true;
        return false;
    }
}
