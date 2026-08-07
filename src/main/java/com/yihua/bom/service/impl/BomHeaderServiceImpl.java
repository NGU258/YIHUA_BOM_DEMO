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

        //这里因为设计了历史版本 所以在添加之前需要加点版本控制相关的逻辑
        //逻辑上需要先判断草稿状态  如果先判断启用状态就会一直重复生成草稿状态的BOM 这样就不对了
        // 1. 如果这个物料在BomHeader里面有草稿状态  就直接操作它
        // 2. 如果这个物料在BomHeader里面没有草稿但有启用状态  则生成一个草稿状态 并将用户的更改填上去
        // 3. 如果这个物料既没有草稿也没有启用但有停用 则抛出异常提醒用户需要启用它才能执行相关的操作
        // 4. 如果物料在BomHeader里面没有上面的这些状态 才插入进来 默认状态为草稿

        //校验一： 在整个BomHeader表中，BOM编码不能重复
        LambdaQueryWrapper<BomHeader> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomHeader::getBomCode,b.getBomCode());
        if(!Objects.isNull(getOne(lqw)))
            throw new fairyCatException("500","填入的BOM编码在数据库中已有重");

        BomHeader bomHeader = new BomHeader();
        BeanUtils.copyProperties(b,bomHeader);

            //1. 先把一些必要的字段先存进来
            //1.1 如果用户给了productId 需要回填对应的product_code跟product_name
            if(!Objects.isNull(bomHeader.getProductId())){
                //就通过物料Id来自动填充对应的物料编码、物料名称
                Material m = iMaterialService.getById(bomHeader.getProductId());
                if(Objects.isNull(m))
                    throw new fairyCatException("400","这个物料在物料表中不存在");
                bomHeader.setProductCode(m.getMaterialCode());
                bomHeader.setProductName(m.getMaterialName());
            }

            //1.2 如果用户没有设置BOM类型的话就给个默认值EBOM
            if(Objects.isNull(bomHeader.getBomType()))
                bomHeader.setBomType(BomType.EBOM.getValue());

            //2. 把物料对应所有的BOM状态都查出来
            lqw.clear();
            lqw.eq(BomHeader::getProductId,bomHeader.getProductId());
            List<BomHeader> bomHeaderList = list(lqw);

            //4.1 如果一个状态都没有 就插入它 默认设置成草稿状态
            if(Objects.isNull(bomHeaderList)) {
                //这里用户传入的DTO是没有id的 所以这里可以不给id置为null
                //设置成草稿状态
                bomHeader.setStatus(BomStatus.DRAFT.getValue());
                bomHeader.setIsDefault(0);

                Boolean result = saveOrUpdate(bomHeader);
                if(!result)
                    throw new fairyCatException("500","Bom主表插入失败");
                //然后把这个插入的数据显示给用户看
                return bomHeader;
            }else {//物料有BOM状态的情况
                boolean active = false;
                boolean disabled = false;
                BomHeader bomHeaderCp = null; //保存非草稿状态的BOM
                for (BomHeader cur : bomHeaderList) {

                    if (BomStatus.DRAFT.getValue().equalsIgnoreCase(cur.getStatus())) {
                        //1.1 如果有草稿状态的BOM 优先操作它 草稿状态的话不需要升版 只有在遇到启用状态后生成的草稿BOM才需要生版

                        //把用户的一些更改同步上去
                        if(!Objects.isNull(bomHeader.getBomType()))
                            cur.setBomType(bomHeader.getBomType());

                        if(!Objects.isNull(bomHeader.getBaseQty()))
                            cur.setBaseQty(bomHeader.getBaseQty());

                        if(!Objects.isNull(bomHeader.getUnit()))
                            cur.setUnit(bomHeader.getUnit());

                        if(!Objects.isNull(bomHeader.getEffectiveDate()))
                            cur.setEffectiveDate(bomHeader.getEffectiveDate());

                        if(!Objects.isNull(bomHeader.getExpireDate()))
                            cur.setExpireDate(bomHeader.getExpireDate());

                        if(!Objects.isNull(bomHeader.getRemark()))
                            cur.setRemark(bomHeader.getRemark());

                        Boolean result = iBomHeaderService.saveOrUpdate(cur);
                        if (!result)
                            throw new fairyCatException("500", "更新草稿状态失败");
                        return cur;
                    } else if (BomStatus.ACTIVE.getValue().equalsIgnoreCase(cur.getStatus())) {
                        //2.1 找到一个启用状态的BOM  逻辑是会生成一个新草稿BOM 然后返回这个草稿BOM的Id
                        //这里做一个标记 防止第一次没有草稿 但第二次循环就有了
                        active = true;
                        bomHeaderCp = cur;
                    } else {
                        //停用状态
                        disabled = true;
                        bomHeaderCp = cur;
                    }
                }
                if (active) {
                    //2.2 如果没有草稿状态但有启用状态 优先处理它 生成一个草稿状态的BOM出来  它的id设置成这个草稿状态BOM的id
                    Long draftId = iBomHeaderService.returnDraftBomIdByBomMaterialId(bomHeader.getProductId());
                    //找到那个已升版的草稿记录
                    BomHeader bomHeaderDraft = iBomHeaderService.getById(draftId); //这里就是获取那个草稿状态的记录 这里必有值 就可以不用判断了

                    //把用户的一些更改同步上去
                    if(!Objects.isNull(bomHeader.getBomType()))
                        bomHeaderDraft.setBomType(bomHeader.getBomType());

                    if(!Objects.isNull(bomHeader.getBaseQty()))
                        bomHeaderDraft.setBaseQty(bomHeader.getBaseQty());

                    if(!Objects.isNull(bomHeader.getUnit()))
                        bomHeaderDraft.setUnit(bomHeader.getUnit());

                    if(!Objects.isNull(bomHeader.getEffectiveDate()))
                        bomHeaderDraft.setEffectiveDate(bomHeader.getEffectiveDate());

                    if(!Objects.isNull(bomHeader.getExpireDate()))
                        bomHeaderDraft.setExpireDate(bomHeader.getExpireDate());

                    if(!Objects.isNull(bomHeader.getRemark()))
                        bomHeaderDraft.setRemark(bomHeader.getRemark());

                    //2.3 这样草稿BOM的升版与用户修改的状态就都合并在一起了
                    Boolean result = saveOrUpdate(bomHeaderDraft);
                    if (!result)
                        throw new fairyCatException("500", "更新草稿状态BOM时出错");
                    return bomHeaderDraft;
                } else {
                    //3.1 没有启用与草稿 但有停用
                    //则提示用户需要去启用停用状态的BOM才可以操作生成新的草稿状态
                    throw new fairyCatException("500", "检测到该物料存在一个停用状态的BOM【bomId:" + bomHeaderCp.getId() + "】，请先启用它后再执行当前操作");
                }
            }//有BOM状态的相关逻辑结束
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

        //对树的根节点进行更新操作 跟创建操作的逻辑类似
        //逻辑上需要先判断草稿状态  如果先判断启用状态就会一直重复生成草稿状态的BOM 这样就不对
        // 1. 如果这个物料在BomHeader里面有草稿状态  则更新它
        // 2. 如果这个物料在BomHeader里面没有草稿但有启用状态  生成一个草稿状态 并将用户的更改填上去
        // 3. 如果这个物料既没有草稿也没有启用但有停用 则抛出异常提醒用户需要启用它才能生成对应的草稿状态进行更新它
        // 4. 如果物料在BomHeader里面没有上面的这些状态 就抛出对应的异常信息


        //校验规则一： 在整个BomHeader表中，BOM编码不能重复
        LambdaQueryWrapper<BomHeader> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomHeader::getBomCode,b.getBomCode());
        if(!Objects.isNull(getOne(lqw)))
            throw new fairyCatException("500","填入的BOM编码在数据库中已有重");

        //1. 先拿出要被更新的主表记录
        BomHeader bomHeader = getById(bomId);
        BeanUtils.copyProperties(b,bomHeader);
        if(Objects.isNull(bomHeader))
            throw new fairyCatException("500","数据库Bom主表中未查到该记录["+bomId+"]");

        if(!Objects.isNull(b.getProductId())){
            //如果用户更改了物料id 则回填相应的值
            Material material = iMaterialService.getById(b.getProductId());
            if(Objects.isNull(material))
                throw new fairyCatException("400","填入的物料id有误，在物料表中并未存在");
            bomHeader.setProductCode(material.getMaterialCode());
            bomHeader.setProductName(material.getMaterialName());
        }

        //2. 把物料对应所有的BOM状态都查出来
        lqw.clear();
        lqw.eq(BomHeader::getProductId,bomHeader.getProductId());
        List<BomHeader> bomHeaderList = list(lqw);

        //4.1 如果一个状态都没有 就抛出异常
        if(Objects.isNull(bomHeaderList))
            throw new fairyCatException("400","更新失败，该物料没有对应的BOM，请联系管理员添加");

        boolean active = false;
        boolean disabled = false;
        BomHeader bomHeaderCp = null;
        //遍历所有的BOM状态
        for(BomHeader cur : bomHeaderList) {

            if(BomStatus.DRAFT.getValue().equalsIgnoreCase(cur.getStatus())) {
                //1.1 如果是草稿的 优先更新它

                //把用户的一些更改同步上去
                if(!Objects.isNull(bomHeader.getBomType()))
                    cur.setBomType(bomHeader.getBomType());

                if(!Objects.isNull(bomHeader.getBaseQty()))
                    cur.setBaseQty(bomHeader.getBaseQty());

                if(!Objects.isNull(bomHeader.getUnit()))
                    cur.setUnit(bomHeader.getUnit());

                if(!Objects.isNull(bomHeader.getEffectiveDate()))
                    cur.setEffectiveDate(bomHeader.getEffectiveDate());

                if(!Objects.isNull(bomHeader.getExpireDate()))
                    cur.setExpireDate(bomHeader.getExpireDate());

                if(!Objects.isNull(bomHeader.getRemark()))
                    cur.setRemark(bomHeader.getRemark());

                Boolean result = iBomHeaderService.saveOrUpdate(cur);
                if(!result)
                    throw new fairyCatException("500","更新草稿状态失败");
                return cur;
            }else if(BomStatus.ACTIVE.getValue().equalsIgnoreCase(cur.getStatus())){
                //2.1 启用状态  会生成一个新草稿BOM 然后返回草稿BOM的Id
                //这里做一个标记 防止第一次没有草稿 但第二次循环就有了
                active = true;
                bomHeaderCp = cur ;
            }else{
                //停用状态
                disabled = true;
                bomHeaderCp = cur;
            }
        }
        if(active){
            //2.2 如果没有草稿状态但有启用状态 则先处理它 生成一个草稿状态的BOM出来  它的id设置成这个草稿状态BOM的id
            Long draftId = iBomHeaderService.returnDraftBomIdByBomMaterialId(bomHeader.getProductId());
            //找到那个已升版的草稿记录
            BomHeader bomHeaderDraft = iBomHeaderService.getById(draftId); //这里就是获取那个草稿状态的记录 这里必有值 就可以不用判断了

            //把用户的一些更改同步上去
            if(!Objects.isNull(bomHeader.getBomType()))
                bomHeaderDraft.setBomType(bomHeader.getBomType());

            if(!Objects.isNull(bomHeader.getBaseQty()))
                bomHeaderDraft.setBaseQty(bomHeader.getBaseQty());

            if(!Objects.isNull(bomHeader.getUnit()))
                bomHeaderDraft.setUnit(bomHeader.getUnit());

            if(!Objects.isNull(bomHeader.getEffectiveDate()))
                bomHeaderDraft.setEffectiveDate(bomHeader.getEffectiveDate());

            if(!Objects.isNull(bomHeader.getExpireDate()))
                bomHeaderDraft.setExpireDate(bomHeader.getExpireDate());

            if(!Objects.isNull(bomHeader.getRemark()))
                bomHeaderDraft.setRemark(bomHeader.getRemark());

            //2.3创建了一个草稿状态的BOM之后
            //再把用户传入过来的更改同步到这个草稿状态上
            Boolean result = saveOrUpdate(bomHeaderDraft);
            if(!result)
                throw new fairyCatException("500","更新草稿状态BOM时出错");
            return bomHeaderDraft;
        }else{
            //3.1 没有启用与草稿 但有停用
            //则提示用户需要去启用停用状态的BOM才可以操作生成新的草稿状态
            throw new fairyCatException("500","该物料存在一个停用状态的BOM【bomId:"+bomHeaderCp.getId()+"】，请先去启用它才能执行更新操作");
        }
    }

    @Override
    @Transactional
    public Map<String, Object> deleteBomHeaderAndItemByBomId(Long bomId) {

        //删除的话想了下好像也不需要版本控制 直接删掉就可以了
        //如果有草稿 就操作草稿
        //如果没有草稿有启用 就生成对应的草稿BOM 然后操作这个草稿BOM
        //如果没有草稿没有启用有停用 就提示用户需要启用它才能执行删除逻辑

        //1.定义一个存放删除结果的Map
        Map<String,Object> map = new HashMap<>();

        //2.校验下BOM主表中有没有这个bomId
        BomHeader bomHeader = getById(bomId);
        if(Objects.isNull(bomHeader))
            throw new fairyCatException("500","未在BOM主表中查询到相应记录");

//        //3.根据这条记录的物料id判断是否需要生成对应草稿状态的BOM 最终都会返回要操作的那个草稿BOM的id
//        Long draftId = returnDraftBomIdByBomMaterialId(bomHeader.getProductId());

        //3.删除草稿主表记录
        Boolean headerResult = removeById(bomId);
        if(!headerResult)
            throw new fairyCatException("500","删除BOM主表记录失败");

        //4.接着找所有的子表记录
        LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomItem::getBomId,bomId);

        //这里用于返回给用户删掉的数据
        List<BomItem> bomItems = iBomItemService.list(lqw);

        map.put("BomHeader",bomHeader);

        //如果明细表里面没有记录的话直接返回就可以了
        //只需要让用户看到的JSON中有已经删掉的主表记录跟子表的地方显示[]就可以了
        if(Objects.isNull(bomItems))
            return map;

        //如果有的话就继续删除草稿子表明细
        Boolean itemResult  = iBomItemService.remove(lqw);
        if(!itemResult)
            throw new fairyCatException("500","批量删除BOM明细失败");

        map.put("BomItem",bomItems);
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
    public Long getActiveBomHeaderIdByMaterialId(Long materialId) {
        //这里需要拿一下物料在bomHeader中启用或草稿状态的BOMHeader对应的id 方便后面根据查树BOM响应结果中的id进行快速测试(启用与停用 就不需要自己老是去找了)
        //如果没有启用版本则在设计上默认就拿草稿状态，而草稿状态只会有一个，启用状态也只会有一个 剩余的就是停用状态
        LambdaQueryWrapper<BomHeader> lqw_h = new LambdaQueryWrapper<>();
        lqw_h.eq(BomHeader::getProductId,materialId)
                .eq(BomHeader::getStatus,BomStatus.ACTIVE.getValue());
        BomHeader bomHeader_active = getOne(lqw_h);
        Long bomHeaderId = bomHeader_active != null ? bomHeader_active.getId(): null; //默认先拿启用状态的
        if(Objects.isNull(bomHeaderId)){
            //启用状态没有默认就拿其它状态的
            lqw_h.clear();
            lqw_h.eq(BomHeader::getProductId,materialId);
            List<BomHeader> bomHeader_draft =list(lqw_h);
            //如果还是null的话就说明这个物料是原材料  赋值为null就可以了 null就代表原材料的意思
            bomHeaderId = bomHeader_draft != null ? bomHeader_draft.get(0).getId() : null;
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


        Map<String,Object> hash = new HashMap<>();
        hash.put("草稿状态Id",returnDraftBomIdByBomMaterialId(bomId));

        return hash;
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

    //传入一个物料id 返回它草稿状态的BOMId 如果已经有草稿状态就直接返回  如果没有就先创建一个草稿状态的BOM再返回它的ID
    @Override
    public Long returnDraftBomIdByBomMaterialId(Long materialId) {

        //整体思路
        //在我的整个设计中BOM的草稿状态跟启用状态一样只会有一个

        //这里不管用户传啥MaterialId 都会先去找那个草稿状态的BOM  如果是先找那个启用状态的就不对了 每次请求一次都会复制出来新的草稿状态
        //如果有草稿状态的BOM 就直接返回这个草稿状态的BOMid
        //如果没有 分三种情况
        //情况一： 如果没有草稿状态有启用状态的BOM  就生成一个对应的草稿BOM 然后返回这个草稿BOM的bomId
        //情况二： 如果没有草稿状态没有启用状态但有停用状态  就提示用户需要启用它 因为草稿状态只有在启用状态的前提下才会生成
        //情况三： 如果上面的状态都没有就直接抛异常提示用户这个物料没有对应的Bom就可以了

        //1. 先判断用户传入的这个物料Id在物料表中是否存在
        LambdaQueryWrapper<Material> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Material::getId,materialId);
        Material material = iMaterialService.getOne(lqw);
        if(Objects.isNull(material))
            throw new fairyCatException("400","填入的物料id有误，请填入一个正确的物料id");

        //2. 拿这个物料id去BomHeader表里面找一下对应的启用状态的bom
        //优化思路： 先把所有的状态都找出来  再遍历找对应启用状态的BOM
        LambdaQueryWrapper<BomHeader> lqw_header = new LambdaQueryWrapper<>();
        lqw_header.eq(BomHeader::getProductId,materialId);
        List<BomHeader> bomHeaderList = list(lqw_header);
        if(Objects.isNull(bomHeaderList)) //如果一个BOM状态都没有 就是情况三 直接抛出异常提示用户就行了
            throw new fairyCatException("500","该物料没有对应的BOM，请联系管理员添加");

        //2.1 接着遍历所有的BOM状态 进行相关的逻辑处理
        //这里需要对启用与停用状态分别进行标记 这里不能满足条件后就结束了 需要继续往后遍历看有没有草稿 因为数据库是一条一条记录的看的
        boolean disabled = false;
        boolean active = false;
        BomHeader bomHeaderCp = null;
        for(BomHeader curH: bomHeaderList){
            //先判断草稿状态
            if(BomStatus.DRAFT.getValue().equalsIgnoreCase(curH.getStatus())){
                //如果是草稿状态 直接返回它的bomId就可以了
                return curH.getId();
            }else if(BomStatus.ACTIVE.getValue().equalsIgnoreCase(curH.getStatus())){
                //启用状态 如果遍历到的话不能直接返回  需要继续往后看还有没有草稿状态
                //如果没有才需要生成它对应的草稿BOM出来 并返回它的草稿BOM状态Id
                bomHeaderCp = curH;
                active = true;
            }else{
                //停用状态
                disabled = true;
            }
        }
        if(active){
            //如果有启用状态的 直接生成对应的草稿BOM最后返回草稿ID就可以了
            return returnDraftBomByBomHeader(bomHeaderCp);
        }
        if(disabled) //如果没有启用状态
            throw new fairyCatException("400","当前物料["+materialId+"]对应的BOM没有启用，需要去启用它才可以执行相关操作");

        //这里函数需要一个返回值  但按代码逻辑来说不会到这里 但不写的话会报错
        return -1L;
    }

    @Override
    public Long returnDraftBomByBomHeader(BomHeader bomHeader) {
        //复制成草稿状态的BOM思路 主要是复制BOM主表跟子表

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

                BomHeader r = opHistoryAdd(bomHeader_copy);
                bomHeader_copy.setBomCode(r.getBomCode());
                bomHeader_copy.setBomName(r.getBomName());
                bomHeader_copy.setBomVersion(r.getBomVersion());

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
                //如果是半成品的话这里不好复制对应的子表 主要还是复制成品的子表
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
                        //这里的映射赋值单独做一个循环做  可以理解成是一个防御性编程吧 单独分开不容易出错
                        if(cur.getParentId() != 0l)
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

    @Override
    public BomHeader opHistoryAdd(BomHeader bomHeader) {

        //处理后面的版本数字
        //在正则表达式中 \d表示匹配任意一个数字 等价[0-9]
        //Java中String的format方法 等价于C中的printf方法 语法几乎一样
        //处理BomCode
        String opStr = bomHeader.getBomCode(); //BOM-A-0001

            Matcher matcher = Pattern.compile("(\\d+)$").matcher(opStr);
            String opStrResult = "";
            if(matcher.find())
                opStrResult = matcher.group(1);//这里就变成0001了
            //再转成数字 在转化之前需要判空字符串 因为group方法如果没匹配到返回的就是空字符串
            if(opStrResult.equals(""))
                opStr = bomHeader.getBomCode();//如果没有匹配成功 恢复原值
            else {
                int newVersionNum = Integer.parseInt(opStrResult) + 1; //版本数字+1

                //先将拼接的这个版本数字2 转换成0002的形式
                String replaceStr = String.format("%04d", newVersionNum);
                //接着把原字符串中的000X版本字符串替换成这个新的
                opStr = opStr.replace(opStrResult,replaceStr);
            }

        bomHeader.setBomCode(opStr);

        //处理BomName 示例： BOM-成品A-v1
        String bomName = bomHeader.getBomName();
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
                bomName = bomHeader.getBomName();
            else{
                int versionNum = Integer.parseInt(processStr);
                int newVersionNum = versionNum + 1;

                //这里的写法是为了防止数据库中的值有v或V
                //这里replace方法的逻辑是如果找到了就替换  如果没有找到就返回原字符串
                bomName = bomName.replace("v"+versionNum,"v"+newVersionNum);
                bomName = bomName.replace("V"+versionNum,"V"+newVersionNum);

            }
        bomHeader.setBomName(bomName);

        //bomVersion
        String bomVersion = bomHeader.getBomVersion(); //V1
        //这里就直接截取后面的数字就可以了
            int newVersionNum = Integer.parseInt(bomVersion.substring(1))+1;

            bomVersion = "V"+newVersionNum ;

        bomHeader.setBomVersion(bomVersion);

        return bomHeader;
    }
}