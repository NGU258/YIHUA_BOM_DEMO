package com.yihua.bom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yihua.bom.Mapper.BomHeaderMapper;
import com.yihua.bom.constants.Enum.bom.BomStatus;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class BomHeaderServiceImpl extends ServiceImpl<BomHeaderMapper, BomHeader> implements IBomHeaderService {

    private final IBomItemService iBomItemService;
    private final IMaterialService iMaterialService;

    public BomHeaderServiceImpl(IBomItemService ib,IMaterialService im){
        this.iBomItemService = ib;
        this.iMaterialService = im;
    }
    @Override
    @Transactional
    public BomHeader createBomHeader(BomHeaderDTO b) {

        BomHeader bomHeader = new BomHeader();
        BeanUtils.copyProperties(b,bomHeader);

        //因为这里已经知道product_id了 所以我需要回填对应的product_code跟product_name
        BomHeader bh = getById(bomHeader.getProductId());

        bomHeader.setProductCode(bh.getProductCode());
        bomHeader.setProductName(bh.getProductName());

        Boolean result = saveOrUpdate(bomHeader);

        if(!result)
            throw new fairyCatException("500","插入失败");
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

        if(StringUtils.hasText(b.getProductId().toString())){
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

        Boolean result = updateById(bomHeader);
        if(!result)
            throw new fairyCatException("500","启用BOM失败，请联系管理员");

       return BomHeaderVo.builder()
                .id(bomHeader.getId())
                .bomName(bomHeader.getBomName())
                .bomCode(bomHeader.getBomCode())
                .status(bomHeader.getStatus())
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

        Boolean result = updateById(bomHeader);
        if(!result)
            throw new fairyCatException("500","停用BOM失败，请联系管理员");

        BeanUtils.copyProperties(bomHeader,bomHeaderVo);

        return bomHeaderVo;
    }
}
