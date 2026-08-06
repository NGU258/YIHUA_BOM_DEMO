package com.yihua.bom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yihua.bom.Mapper.BomHeaderMapper;
import com.yihua.bom.constants.Enum.bom.BomStatus;
import com.yihua.bom.constants.Enum.bom.BomType;
import com.yihua.bom.dto.BomHeaderDTO;
import com.yihua.bom.entity.BomHeader;
import com.yihua.bom.entity.BomItem;
import com.yihua.bom.entity.Material;
import com.yihua.bom.exception.fairyCatException;
import com.yihua.bom.service.IBomHeaderService;
import com.yihua.bom.service.IBomItemService;
import com.yihua.bom.service.IMaterialService;
import com.yihua.bom.vo.BomHeaderVo;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BomHeaderServiceImpl extends ServiceImpl<BomHeaderMapper, BomHeader> implements IBomHeaderService {

    private final IBomItemService iBomItemService;

    private final IMaterialService iMaterialService;

    private final IBomHeaderService iBomHeaderService;

    public BomHeaderServiceImpl(@Lazy IBomItemService ib, @Lazy IMaterialService ims,@Lazy IBomHeaderService ibs){
        this.iBomItemService = ib;
        this.iMaterialService = ims;
        this.iBomHeaderService = ibs;
    }
    @Override
    @Transactional
    public BomHeader createBomHeader(BomHeaderDTO b) {

        if(Objects.isNull(b.getBomCode())||Objects.isNull(b.getProductId()))
            throw new fairyCatException("400","BOM编码跟产品ID不能为空");

        BomHeader bomHeader = new BomHeader();
        BeanUtils.copyProperties(b,bomHeader);

        LambdaQueryWrapper<BomHeader> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(BomHeader::getBomCode,b.getBomCode());
        if(!Objects.isNull(getOne(lqw1)))
            throw new fairyCatException("500","BOM编码已重复");

        //因为这里已经知道product_id了 所以我需要回填对应的product_code跟product_name
        BomHeader bh = getById(bomHeader.getProductId());
        bomHeader.setProductCode(bh.getProductCode());
        bomHeader.setProductName(bh.getProductName());
        //这个地方就比较关键了 后面判断这个物料的BOM是否需要展开就得用这个字段
        bomHeader.setIsDefault(1);
        bomHeader.setId(null);
        //如果用户没有设置BOM类型的话就给个默认值
        if(Objects.isNull(bomHeader.getBomType()))
            bomHeader.setBomType(BomType.EBOM.getValue());

            //这里因为设计了历史版本
            //所以在添加之前需要进行判断
            //这里需要先判断草稿状态  如果先判断的是启用状态的话又会重新生成一个新的草稿状态 这样就不对了
            // 1. 如果这个物料在BomHeader里面有草稿状态  则更新它
            // 2. 如果这个物料在BomHeader里面有启用状态  则生成一个草稿状态
            // 3. 如果这个物料在BOmHeader里面没有上面的这些状态 才插入它

            //先判断草稿
            LambdaQueryWrapper<BomHeader> lqw =  new LambdaQueryWrapper<>();
            lqw.eq(BomHeader::getProductId,bomHeader.getProductId())
                    .eq(BomHeader::getStatus,BomStatus.DRAFT.getValue());
            BomHeader bomHeader1 = getOne(lqw);

            if(Objects.isNull(bomHeader1)){
                //如果没有草稿状态  再看启用
                lqw.clear();
                lqw.eq(BomHeader::getProductId,bomHeader.getProductId())
                        .eq(BomHeader::getStatus,BomStatus.ACTIVE.getValue());
                BomHeader bomHeader2 = getOne(lqw);
                //如果都没有 才需要插入
                if(Objects.isNull(bomHeader2)){
                    Boolean result = saveOrUpdate(bomHeader);
                    if(!result)
                        throw new fairyCatException("500","插入失败");
                    return bomHeader;
                }

                //这里就是启用状态 启用状态就生成一个新的草稿BOM
                //这里是直接返回那个草稿状态的id 如果是启用的也会返回一个草稿状态对应的BomId
                iBomHeaderService.returnDraftBomIdByBomMaterialId(bomHeader.getProductId());

            }


            Boolean result = iBomHeaderService.saveOrUpdate(bomHeader1);
            if(!result)
                throw new fairyCatException("500","更新草稿状态失败");

        return bomHeader;
    }

    @Override
    public IPage<BomHeader> listBomHeader(Long curPage, Long curPageNum, String keyword) {
        Page<BomHeader> page = new Page<>(curPage, curPageNum);

        LambdaQueryWrapper<BomHeader> lqw = new LambdaQueryWrapper<>();
        //可以查BOM编码或者BOM名称
        if(!Objects.isNull(keyword)){
            lqw.like(BomHeader::getBomCode,keyword)
                    .or()
                    .like(BomHeader::getBomName,keyword);
        }

        lqw.orderByDesc(BomHeader::getCreateTime);

        return  baseMapper.selectPage(page, lqw);
    }

    @Override
    public Map<String, Object> getBomHeaderAndItemsByBomId(Long bomId) {
        BomHeader bomHeader = getById(bomId);
        if(Objects.isNull(bomHeader))
            throw new fairyCatException("500","数据库中未查询到该BOM");

        List<BomItem> bomItems = iBomItemService.getDetailsById(bomId);

        Map<String, Object> bomHeaderItemVo = new HashMap<>();

        //这里因为泛型的不可变性 所以List<Object>是没法接收List<Header>的
        //所以最佳实践是直接放一个Object就可以了
        bomHeaderItemVo.put("bomData",bomHeader);
        bomHeaderItemVo.put("bomItemData",bomItems);

        return bomHeaderItemVo;
    }

    @Override
    @Transactional
    public BomHeader updateBomHeaderByBomId(Long bomId, BomHeaderDTO b) {
        BomHeader bomHeader = getById(bomId);
        if(Objects.isNull(bomHeader))
            throw new fairyCatException("500","数据库Bom主表中未查到该记录");
        BeanUtils.copyProperties(b,bomHeader);

        if(!Objects.isNull(b.getProductId())){
            //如果物料id有值 我才需要进行回填
            Material material = iMaterialService.getById(b.getProductId());
            bomHeader.setProductCode(material.getMaterialCode());
            bomHeader.setProductName(material.getMaterialName());
        }
        Boolean result = updateById(bomHeader);
        if(!result)
            throw new fairyCatException("500","保存失败");
        return bomHeader;
    }

    @Override
    @Transactional
    public Map<String, Object> deleteBomHeaderAndItemByBomId(Long bomId) {

        Map<String,Object> map = new HashMap<>();

        BomHeader bomHeader = getById(bomId);
        if(Objects.isNull(bomHeader))
            throw new fairyCatException("500","未在BOM主表中查询到相应记录");

        LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomItem::getBomId,bomId);

        List<BomItem> bomItems = iBomItemService.list(lqw);

        Boolean headerResult = removeById(bomId);
        if(!headerResult)
            throw new fairyCatException("500","删除BOM主表记录失败");
        map.put("已删除的BomData",bomHeader);
        map.put("已删除的BomDetails",bomItems);

        //如果明细表里面没有记录的话直接返回就可以了
        //只需要让用户看到的JSON中有已经删掉的主表记录跟子表的地方显示[]就可以了
        BomItem bomItem = iBomItemService.getById(bomId);
        if(Objects.isNull(bomItem))
            return map;

        Boolean itemResult  = iBomItemService.remove(lqw);
        if(!itemResult)
            throw new fairyCatException("500","批量删除BOM明细失败");

        return map;
    }

    @Override
    @Transactional
    public BomHeaderVo activeBomStatus(Long bomId) {

        BomHeader bomHeader = getById(bomId);
        if(Objects.isNull(bomHeader))
            throw new fairyCatException("500","在启用BOM时发现要找的BOM在数据库中并没有记录");

        bomHeader.setStatus(BomStatus.ACTIVE.getValue());
        //这是的逻辑就是如果它是启用状态 则代表它是一个默认版本 用1来标识
        bomHeader.setIsDefault(1);

        //启用了当前产品的这个BOM后 其它当前产品的BOM将被停用
        LambdaQueryWrapper<BomHeader> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomHeader::getProductId,bomHeader.getProductId());

        List<BomHeader> bomHeaderList = iBomHeaderService.list(lqw);

        for(BomHeader cur: bomHeaderList){
            //如果是自己 则跳过
            if(cur.getId().equals(bomHeader.getId()))
                continue;
            //其它情况需要将状态改成DRAFT 然后is_Default改成0
            cur.setStatus(BomStatus.DISABLED.getValue());
            cur.setIsDefault(0);
            Boolean result = iBomHeaderService.updateById(cur);
            if(!result)
                throw new fairyCatException("500","在启用该产品BOM时发现停用该产品其它BOM版本时出错，请联系管理员");
        }

        Boolean result = updateById(bomHeader);
        if(!result)
            throw new fairyCatException("500","启用BOM失败，请联系管理员");

       return BomHeaderVo.builder()
                .materialId(bomHeader.getProductId())
                .materialCode(bomHeader.getProductCode())
                .materialName(bomHeader.getProductName())
                .status(bomHeader.getStatus())
                .isDefault(bomHeader.getIsDefault())
                .bomCode(bomHeader.getBomCode())
                .bomName(bomHeader.getBomName())
                .id(bomHeader.getId())
                .build();
    }

    @Override
    @Transactional
    public BomHeaderVo disableBomStatus(Long bomId) {
        BomHeader bomHeader = getById(bomId);
        if(Objects.isNull(bomHeader))
            throw new fairyCatException("500","当前BOM在数据库中不存在");

        BomHeaderVo bomHeaderVo = new BomHeaderVo();

        bomHeader.setStatus(BomStatus.DISABLED.getValue());
        bomHeader.setIsDefault(0);

        Boolean result = updateById(bomHeader);
        if(!result)
            throw new fairyCatException("500","停用BOM失败，请联系管理员");

        BeanUtils.copyProperties(bomHeader,bomHeaderVo);
        bomHeaderVo.setMaterialCode(bomHeader.getProductCode());
        bomHeaderVo.setMaterialName(bomHeader.getProductName());
        bomHeaderVo.setMaterialId(bomHeader.getProductId());

        return bomHeaderVo;
    }

    @Override
    public Long getBomHeaderIdByMaterialId(Long materialId) {
        //这里需要拿一下物料在bomHeader中启用或草稿状态的BOMHeader对应的id 方便后面根据查树BOM响应结果中的id进行快速测试(启用与停用 就不需要自己老是去找了)
        //如果没有启用版本则在设计上默认就拿草稿状态，而草稿状态只会有一个，启用状态也只会有一个 剩余的就是停用状态
        LambdaQueryWrapper<BomHeader> lqw_h = new LambdaQueryWrapper<>();
        lqw_h.eq(BomHeader::getProductId,materialId)
                .eq(BomHeader::getStatus,BomStatus.ACTIVE.getValue());
        BomHeader bomHeader_active = getOne(lqw_h);
        Long bomHeaderId = bomHeader_active != null ? bomHeader_active.getId(): null; //默认先拿启用状态的
        if(Objects.isNull(bomHeaderId)){
            //启用状态没有默认就拿草稿状态的
            lqw_h.clear();
            lqw_h.eq(BomHeader::getProductId,materialId)
                    .eq(BomHeader::getStatus,BomStatus.DRAFT.getValue());
            BomHeader bomHeader_draft =getOne(lqw_h);
            //如果还是null的话就说明这个物料是原材料  赋值为null就可以了 null就代表原材料的意思
            bomHeaderId = bomHeader_draft != null ? bomHeader_draft.getId() : null;
        }
        return bomHeaderId;
    }

    //历史版本功能的设计与实现
    // 思路： 每个物料只会对应着一个草稿状态BOM、一个启用状态BOM、其余的都是停用状态BOM
    // 用户的每次更新都只会操作草稿状态的那个BOM  而查询它树结构的时候只会展开启用状态BOM的那个树结构
    // 只有当用户启用了这个BOM 之后 去查树结构的时候才能看到
    // 1.写一个生产草稿BOM的方法
    @Override
    public Map<String, Object> copyBomHeaderAndBomItemByBomId(Long bomId) {


        returnDraftBomIdByBomMaterialId(bomId);

        return null;
//        //复制成功后再返回给用户看
//        Map<String,Object> hashMap = new HashMap<>();
//        //思路： 传入一个bomId 然后把它的主表跟子表全部复制出来
//
//        //复制主表需要看的字段如下(其它的直接正常拷贝就可以了)
//        //bomCode 版本数字+1
//        //bomName 版本数字+1
//        //bomVersion 版本数字+1
//        //status 状态变成草稿
//        //isDefault 默认启用设置成0
//        BomHeader bomHeader = getById(bomId);
//        if(Objects.isNull(bomHeader))
//            throw new fairyCatException("400","填入了一个错误的bomId,该bomId在Bom主表中并不存在");
//
//        //先复制下主表
//        BomHeader bomHeader_copy = new BomHeader();
//        BeanUtils.copyProperties(bomHeader,bomHeader_copy);
//
//        //处理版本数字
//        //Pattern.compile(".*?-(0-9+)")
//        //在正则表达式中 \d表示匹配任意一个数字 等价[0-9]
//        //Java中String的format方法 等价于C中的printf方法 语法几乎一样
//        //处理BomCode
//        String opStr = bomHeader_copy.getBomCode(); //BOM-A-0001
//
//            Matcher matcher = Pattern.compile("(\\d+)$").matcher(opStr);
//            String opStrResult = "";
//            if(matcher.find())
//                opStrResult = matcher.group(1);//这里就变成0001了
//            //再转成数字 在转化之前需要判空字符串 因为group方法如果没匹配到返回的就是空字符串
//            if(opStrResult.equals(""))
//                  opStr = bomHeader_copy.getBomCode();//如果没有匹配成功 恢复原值
//            else {
//                int newVersionNum = Integer.parseInt(opStrResult) + 1; //版本数字+1
//
//                //先将拼接的这个版本数字2 转换成0002的形式
//                String replaceStr = String.format("%04d", newVersionNum);
//                //接着把原字符串中的000X版本字符串替换成这个新的
//                opStr = opStr.replace(opStrResult,replaceStr);
//            }
//        bomHeader_copy.setBomCode(opStr);
//            //处理BomName 示例： BOM-成品A-v1
//            String bomName = bomHeader_copy.getBomName();
//            //String类的replaceFirst只会替换掉第一个满足条件的
//            //String类的replace有两个重载  一个是替换掉字符 另一个是替换掉字符串
//            //从java9开始Matcher类的replaceAll支持一个函数 这个函数可以是Lambda表达式  而当前的java8是不支持这个Lambda写法的
//            //Lambda表达式中的参数名可自定义 它代表捕获到的这个对象
//            //用.group()方法来获取到匹配的值
//            //在Matcher类的replaceAll这个函数中满足条件就会来调用一次Labmda表达式 然后执行相关的逻辑
//            //记得返回修改后的结果 这样才会把前面匹配的内容替换成它
//            Matcher matchFairyCat = Pattern.compile("(\\d+)$").matcher(bomName);
//            String processStr = "";
//            if(matchFairyCat.find())
//                 processStr = matchFairyCat.group(1); //取到数字1
//            //判断截取后的字符串
//            if(processStr.equals(""))
//                bomName = bomHeader_copy.getBomName();
//            else{
//                int versionNum = Integer.parseInt(processStr);
//                int newVersionNum = versionNum + 1;
//
//                bomName = bomName.replace("v"+versionNum,"v"+newVersionNum);
//            }
////        bomName = matchFairyCat.replaceAll();
//        bomHeader_copy.setBomName(bomName);
//
//        //bomVersion
//        String bomVersion = bomHeader_copy.getBomVersion(); //V1
//        //这里就直接截取后面的数字就可以了
//            int newVersionNum = Integer.parseInt(bomVersion.substring(1))+1;
//
//            bomVersion = "V"+newVersionNum ;
//
//        bomHeader_copy.setBomVersion(bomVersion);
//
//        //处理状态跟默认启用
//        bomHeader_copy.setStatus(BomStatus.DRAFT.getValue());
//        bomHeader_copy.setIsDefault(0);
//
//        //这里也会把id同样复制过去  这样的话直接插入到数据库中会触发主键索引的唯一性机制
//        //所以需要将id置空 这里置空的话Mybatis-Plus会自己生成一个并回推回来
//        bomHeader_copy.setId(null);
//
//        Boolean  result = save(bomHeader_copy);
//        if(!result)
//            throw new fairyCatException("500","保存主表失败");
//
////        System.out.println("BomId: "+bomHeader_copy.getId());
//
//        //复制子表需要更新的字段
//        //bomId 它是连接着根节点的 现在根节点变化了 它也得变化一下
//        //parentId这里也需要维护一下 这里也是复制明细这里最难的地方 思路如下
//            //1. 先直接复制所有明细生成对应的新id
//            //2. 然后再将旧id与新id做个映射
//            //3. 把原来的parentId对应的旧id替换成新id
//
//        hashMap.put("bomHeader:",bomHeader_copy);
//
//        return hashMap;

    }

    @Override
    public Long returnDraftBomIdByBomMaterialId(Long materialId) {

        //整体思路
        //在我的整个设计中BOM的草稿状态跟启用状态一样只会有一个
        //这里不管用户传啥MaterialId 都会先去找那个启用状态ACTIVE的BOM
        //如果有ACTIVE的BOM 就生成一个对应的草稿BOM 然后返回这个草稿BOM的bomId
        //反之分两种情况
        //如果有草稿状态 就直接返回这个草稿BOM的id
        //反之就直接抛异常提示用户这个物料没有对应的Bom就可以了

        //先判断一下用户传入的这个MaterialId在物料表中是否存在
        LambdaQueryWrapper<Material> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Material::getId,materialId);
        Material material = iMaterialService.getOne(lqw);
        if(Objects.isNull(material))
            throw new fairyCatException("400","填入的物料id有误，请填入一个正确的物料id");

        //接着拿这个物料id去BomHeader表里面找一下对应的启用状态的bom
        LambdaQueryWrapper<BomHeader> lqw_header = new LambdaQueryWrapper<>();
        lqw_header.eq(BomHeader::getProductId,materialId)
                .eq(BomHeader::getStatus,BomStatus.ACTIVE.getValue());

        BomHeader bomHeader = getOne(lqw_header);
        if(Objects.isNull(bomHeader)){
            //没有启动状态 那就去找一个对应草稿状态的BOM
            lqw_header.clear();
            lqw_header.eq(BomHeader::getProductId,materialId)
                    .eq(BomHeader::getStatus,BomStatus.DRAFT.getValue());
            BomHeader bomHeader2 = getOne(lqw_header);
            //如果这个物料对应的草稿状态也没有的话
            if(Objects.isNull(bomHeader2))
                throw new fairyCatException("400","该物料没有对应的Bom，请联系管理员添加对应的Bom结构");

            //草稿状态 就直接返回它的bomId就可以了
            return bomHeader2.getId();
        }
        else{
                //启用状态 它对应的头记录就是上面的bomHeader
                //现在需要生成这个启用状态对应的草稿BOM出来

                //复制成草稿状态的BOM思路
                    //复制主表需要看的字段如下(其它的直接正常拷贝就可以了)
                    //bomCode 版本数字+1
                    //bomName 版本数字+1
                    //bomVersion 版本数字+1
                    //status 状态变成草稿
                    //isDefault 默认启用设置成0

                //先复制下主表
                BomHeader bomHeader_copy = new BomHeader();
                BeanUtils.copyProperties(bomHeader,bomHeader_copy);

                //因为前面调用的copyProperties方法也会把id同样复制过去
                //但这样的话直接插入到数据库中会触发主键索引的唯一机制
                //所以需要将id置空 这里置空的话Mybatis-Plus会自己生成一个新id并回填回来
                bomHeader_copy.setId(null);

                //处理版本数字
                //在正则表达式中 \d表示匹配任意一个数字 等价[0-9]
                //Java中String的format方法 等价于C中的printf方法 语法几乎一样
                //处理BomCode
                String opStr = bomHeader_copy.getBomCode(); //BOM-A-0001

                    Matcher matcher = Pattern.compile("(\\d+)$").matcher(opStr);
                    String opStrResult = "";
                    if(matcher.find())
                        opStrResult = matcher.group(1);//这里就变成0001了
                    //再转成数字 在转化之前需要判空字符串 因为group方法如果没匹配到返回的就是空字符串
                    if(opStrResult.equals(""))
                        opStr = bomHeader_copy.getBomCode();//如果没有匹配成功 恢复原值
                    else {
                        int newVersionNum = Integer.parseInt(opStrResult) + 1; //版本数字+1

                        //先将拼接的这个版本数字2 转换成0002的形式
                        String replaceStr = String.format("%04d", newVersionNum);
                        //接着把原字符串中的000X版本字符串替换成这个新的
                        opStr = opStr.replace(opStrResult,replaceStr);
                    }
                bomHeader_copy.setBomCode(opStr);

                //处理BomName 示例： BOM-成品A-v1
                String bomName = bomHeader_copy.getBomName();
                //String类的replaceFirst只会替换掉第一个满足条件的
                //String类的replace有两个重载  一个是替换掉字符 另一个是替换掉字符串
                //从java9开始Matcher类的replaceAll支持一个函数 这个函数可以是Lambda表达式  而当前的java8是不支持这个Lambda写法的
                //Lambda表达式中的参数名可自定义 它代表捕获到的这个对象
                //用.group()方法来获取到匹配的值
                //在Matcher类的replaceAll这个函数中满足条件就会来调用一次Labmda表达式 然后执行相关的逻辑
                //记得返回修改后的结果 这样才会把前面匹配的内容替换成它
                Matcher matchFairyCat = Pattern.compile("(\\d+)$").matcher(bomName);
                String processStr = "";
                if(matchFairyCat.find())
                    processStr = matchFairyCat.group(1); //取到数字1
                //判断截取后的字符串
                if(processStr.equals(""))
                    bomName = bomHeader_copy.getBomName();
                else{
                    int versionNum = Integer.parseInt(processStr);
                    int newVersionNum = versionNum + 1;

                    //这里的写法是为了防止数据库中的值有v或V
                    //这里replace方法的逻辑是如果找到了就替换  如果没有找到就返回原字符串
                    bomName = bomName.replace("v"+versionNum,"v"+newVersionNum);
                    bomName = bomName.replace("V"+versionNum,"V"+newVersionNum);

                }
                //        bomName = matchFairyCat.replaceAll();
                bomHeader_copy.setBomName(bomName);

                //bomVersion
                String bomVersion = bomHeader_copy.getBomVersion(); //V1
                //这里就直接截取后面的数字就可以了
                int newVersionNum = Integer.parseInt(bomVersion.substring(1))+1;

                bomVersion = "V"+newVersionNum ;

                bomHeader_copy.setBomVersion(bomVersion);

                //处理状态跟默认启用
                bomHeader_copy.setStatus(BomStatus.DRAFT.getValue());
                bomHeader_copy.setIsDefault(0);

                Boolean  result = save(bomHeader_copy);
                if(!result)
                    throw new fairyCatException("500","生成草稿状态BOM失败");

                //复制子表需要更新的字段
                //bomId 它是连接着根节点的 现在根节点变化了 它也得变化一下
                //parentId这里也需要维护一下 这里也是复制明细这里最难的地方 思路如下
                    //1. 执行save操作，先把子表明细批量复制插入进去
                    //2. 然后给所有明细对应的bomId设置成前面主表中生成的这个草稿BOM对应的BOMId
                    //3. 将新BomItemId与原来的旧BomItemId用Map做个映射
                    //4. 把原来parentId对应的旧BomItemId替换成新BomItemId
                    //5. 最后再执行更新操作即可

                //映射表
                Map<Long,Long> hash = new HashMap<>();

                //先把原来的子表找出来
                //条件就是前面找到的那条主表记录的id(bomHeader.getId()) 也就是子表中的bomId
                LambdaQueryWrapper<BomItem> bomItemLQW = new LambdaQueryWrapper<>();
                bomItemLQW.eq(BomItem::getBomId,bomHeader.getId());
                List<BomItem> bomItemList = iBomItemService.list(bomItemLQW);

                //这里需要判断一下它有没有子表
                if(!Objects.isNull(bomItemList)){
                    //只有子表有值的时候才需要进行复制 然后遍历它
                    for(BomItem cur : bomItemList){

                        //保存之前设置一下它的BomId 代表是这个树下的
                        BomItem bomItem = new BomItem();
                        BeanUtils.copyProperties(cur,bomItem);
                        bomItem.setBomId(bomHeader_copy.getId());
                        //copy过去的时候记得让id置null 然后让Mybatis-Plus回填一下
                        //不然的话就会触发主键的主键索引(唯一非空 测试的情况是触发了唯一报错)报错
                        bomItem.setId(null);

                        //保存进去 生成新的id
                        Boolean result1 = iBomItemService.save(bomItem);
                        if(!result1)
                            throw new fairyCatException("500","复制Bom子表时出错，请联系程序员");


                        //这里有个细节 保存进去后这个id它会自动回填
                        //所以这里其实我还是可以获取到这个id的
                        //直接做对应的映射就可以了
                        //这里就做好了第3步映射表了 旧Id:新Id
                        hash.put(cur.getId(),bomItem.getId());
                    }

                    //这里接着把新插入的Bom子表都找出来
                    LambdaQueryWrapper<BomItem> newBomItem = new LambdaQueryWrapper<>();
                    newBomItem.eq(BomItem::getBomId,bomHeader_copy.getId());
                    List<BomItem> bomItemNew = iBomItemService.list(newBomItem);

                    //再来一遍 处理映射
                    for(BomItem cur: bomItemNew){

                        //这里的映射已经有了 就开始更新对应的parentId了
                        //但这里需要判断一下parentId是不是0 如果是0的话说明是第二层的节点
                        //第二层的节点其实就不需要去更新了 只有不是第二层的才需要维护它的父子关系
                        //这里的映射赋值必须放在这个循环做  不然有些映射还没加进来就还是空的 就达不到理想的效果了
                        if(cur.getParentId() !=0l)
                            cur.setParentId(hash.get(cur.getParentId()));
                        //再执行一次更新操作复制子表的逻辑就完成了
                        Boolean updateResult = iBomItemService.saveOrUpdate(cur);
                        if(!updateResult)
                            throw new fairyCatException("500","在批量更新维护父子关系的parentId时出错，请联系管理员");
                    }
                }
                //最后再把这个草稿状态的bomheaderId返回就可以了
                return bomHeader_copy.getId();
        }
    }
}