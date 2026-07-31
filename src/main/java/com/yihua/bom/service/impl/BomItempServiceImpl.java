package com.yihua.bom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yihua.bom.Mapper.BomItemMapper;
import com.yihua.bom.entity.BomItem;
import com.yihua.bom.service.IBomItemService;
import org.springframework.stereotype.Service;

@Service
public class BomItempServiceImpl extends ServiceImpl<BomItemMapper, BomItem> implements IBomItemService {
}
