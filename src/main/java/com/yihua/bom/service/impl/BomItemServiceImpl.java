package com.yihua.bom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yihua.bom.Mapper.BomItemMapper;
import com.yihua.bom.constants.Enum.bom.BomIssueType;
import com.yihua.bom.dto.BomItemDTO;
import com.yihua.bom.entity.BomHeader;
import com.yihua.bom.entity.BomItem;
import com.yihua.bom.entity.Material;
import com.yihua.bom.exception.fairyCatException;
import com.yihua.bom.service.IBomHeaderService;
import com.yihua.bom.service.IBomItemService;
import com.yihua.bom.service.IMaterialService;
import com.yihua.bom.util.BeanCopyUtils;
import com.yihua.bom.vo.BomItemVo;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class BomItemServiceImpl extends ServiceImpl<BomItemMapper, BomItem> implements IBomItemService {

    private final IMaterialService iMaterialService;

    private final IBomHeaderService iBomHeaderService;

    public BomItemServiceImpl(@Lazy IMaterialService i, @Lazy IBomHeaderService ib){
        iMaterialService = i;
        iBomHeaderService = ib;
    }

    //这里调用selectList的时候要注意位置
    //如果接收的是一个Wrapper<BomItem> 但我实际传的是LambdaQueryWrapper<BomHeader>就不对了
    //必须得传LambdaQueryWrapper<BomItem>才对
    @Override
    public List<BomItem> getDetailsById(Long bomId) {
        LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomItem::getBomId,bomId)
                .orderByDesc(BomItem::getCreateTime);
        return baseMapper.selectList(lqw);
    }

    @Override
    public BomItem createBomItem(Long bomId, BomItemDTO b) {

        BomItem bomItem = new BomItem();
        if(Objects.isNull(b.getIssueType()))
            b.setIssueType(BomIssueType.NORMAL.getValue());
        BeanUtils.copyProperties(b,bomItem);
        bomItem.setBomId(bomId);

        Long materialId =bomItem.getMaterialId();
        if(materialId!=0 && materialId!=null){
            //如果有物料的话就回填一下对应的字段值
            Material material = iMaterialService.getById(materialId);
            bomItem.setMaterialCode(material.getMaterialCode());
            bomItem.setMaterialName(material.getMaterialName());
            bomItem.setMaterialSpec(material.getSpec());
        }


        //校验规则3
        LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomItem::getBomId,bomItem.getBomId())
                .eq(BomItem::getParentId,bomItem.getParentId())
                .eq(BomItem::getMaterialId,bomItem.getMaterialId());
        if(!Objects.isNull(getOne(lqw)))
            throw new fairyCatException("400","同一父BOM下不能重复添加相同子物料");

        //校验规则6  这里主要从当前节点出发 遍历到根节点 看下是否有materialId是重复的就可以了
        if(theMaterialIsOwn(bomItem.getMaterialId(),bomItem.getParentId()))
            throw new fairyCatException("400","在BOM中不能直接添加自己作为子物料,请传入正确的materialId");

        boolean result = saveOrUpdate(bomItem);
        if(!result)
            throw new fairyCatException("保存Bom明细失败,请联系管理员");

        return bomItem;
    }

    @Override
    public BomItem updateBomItemByBomIdAndBomItemId(Long bomId, Long bomItemId, BomItemDTO b) {

        LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomItem::getBomId,bomId)
                .eq(BomItem::getId,bomItemId);
        BomItem bomItem = getOne(lqw);
        if(Objects.isNull(bomItem))
            throw new fairyCatException("500","未在数据库中查询到该Bom明细");
        //这里有一个坑就是 如果parentId用户没有指定的话b中的parentId值就是null
        //执行下面的copyProperties方法就会把bomItem的parentId(原来有值)给覆盖掉 也变成null了
        //它的逻辑就是把同名属性对应的值都复制过去  不管值是否为null
        BeanUtils.copyProperties(b,bomItem);
//        BeanCopyUtils.copyNonNull(b,bomItem); //解决copyProperties方法把字段值为null的字段也全部赋值过去.

        if(b.getMaterialId()!=0 &&b.getMaterialId()!=null){
            //如果物料id也更新了 则对应的Material相关字段值也得一起同步更新 这样才对
            Material material = iMaterialService.getById(b.getMaterialId());
            bomItem.setMaterialCode(material.getMaterialCode());
            bomItem.setMaterialName(material.getMaterialName());
            bomItem.setMaterialSpec(material.getSpec());
        }

        //saveOrUpdate的判断逻辑
        //首先看当前实体类的getById方法返回值是否为空  如果为空 就执行插入操作
        //如果不为空 再拿这个实体类的id去数据库中查
        //如果查到了 就执行更新操作  如果没查到 就执行插入操作
        //前面的parentId现在已经变成null了 我以为数据库那边也是一样存储的null 但发现并不是 值还是原来的值
        //这里就引出了Mybatis-Plus的默认更新策略: 如果字段值是null 就不会更新它
        boolean result = saveOrUpdate(bomItem);

        if(!result)
            throw new fairyCatException("500","更新明细失败,请联系管理员");

        return bomItem;
    }

    @Override
    public BomItemVo deleteBomItemByBomItemId(Long bomId, Long bomItemId) {

        //删除之前保存一下 用于返回
        LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomItem::getBomId,bomId)
                .eq(BomItem::getId,bomItemId);
        BomItem bomItem = getOne(lqw);

        if(Objects.isNull(bomItem))
            throw new fairyCatException("500","该明细已经在数据库中被删掉了");

        //删除指定Bom明细节点对应的树结构 只需要传它对应的bomItemId就可以了
        BomItemVo bomItemVo= deleteBomItemTreeStructByBomItemId(bomItem.getId());

        return bomItemVo;

    }

    @Override
    public BomItemVo findBomItemTreeStructByBomItemId(Long bomItemId) {

         BomItem  bomItem = getById(bomItemId);
         if(Objects.isNull(bomItem))
             throw new fairyCatException("500","数据库中未找到该BOM明细");

         BomItemVo bomItemVo = new BomItemVo();
         BeanUtils.copyProperties(bomItem,bomItemVo);

         LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
         lqw.eq(BomItem::getParentId,bomItemId);

         List<BomItem> childNode = list(lqw);
        //这里的话还是把单位与数量放前面显示一点会好点
        List<BomItemVo> bomItemVoListVo = new ArrayList<>();
         for(BomItem cur : childNode)
             bomItemVoListVo.add(findBomItemTreeStructByBomItemId(cur.getId()));
          bomItemVo.setChildNode(bomItemVoListVo);

         return bomItemVo;
    }

    @Override
    public BomItemVo deleteBomItemTreeStructByBomItemId(Long bomItemId) {

        BomItem  bomItem = getById(bomItemId);
        if(Objects.isNull(bomItem))
            throw new fairyCatException("500","数据库中未找到该BOM明细");

        BomItemVo bomItemVo = new BomItemVo();
        BeanUtils.copyProperties(bomItem,bomItemVo);

        LambdaQueryWrapper<BomItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BomItem::getParentId,bomItemId);

        List<BomItem> childNode = list(lqw);

        //这里的话还是把单位与数量放前面显示一点会好点
        List<BomItemVo> bomItemVoListVo = new ArrayList<>();
        for(BomItem cur : childNode)
            bomItemVoListVo.add(deleteBomItemTreeStructByBomItemId(cur.getId()));
        bomItemVo.setChildNode(bomItemVoListVo);

        Boolean result = removeById(bomItemId);
        if(!result)
            throw new fairyCatException("500","递归删除明细失败");

        return bomItemVo;
    }

    @Override
    public Boolean theMaterialIsOwn(Long materialId, Long parentId) {

            LambdaQueryWrapper<BomItem> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(BomItem::getId,parentId);
            BomItem bomItem = getOne(lqw1);
            if(Objects.isNull(bomItem))
                throw new fairyCatException("400","传入了一个错误的parentId,请联系管理员");

            if(bomItem.getMaterialId().equals(materialId))
                    return true;

            if(bomItem.getParentId().longValue() == 0l){
                //这里测试时发现没有遍历到根节点  所以还需要再查一下
                LambdaQueryWrapper<BomHeader> lqw2 =new LambdaQueryWrapper<>();
                lqw2.eq(BomHeader::getId,bomItem.getBomId());

                BomHeader bomHeader = iBomHeaderService.getOne(lqw2);
                if(Objects.isNull(bomHeader))
                    throw new fairyCatException("500","在校验规则6中，往根节点出发的路径上发现根节点不存在");
                if(bomHeader.getProductId().equals(materialId))
                    return true;
                else return false;

            }

            return theMaterialIsOwn(materialId,bomItem.getParentId());
    }
}
