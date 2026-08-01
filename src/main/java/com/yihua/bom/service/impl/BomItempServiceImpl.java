package com.yihua.bom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yihua.bom.Mapper.BomItemMapper;
import com.yihua.bom.dto.BomItemDTO;
import com.yihua.bom.entity.BomItem;
import com.yihua.bom.service.IBomItemService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class BomItempServiceImpl extends ServiceImpl<BomItemMapper, BomItem> implements IBomItemService {

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
        return new BomItem();
    }
}
