package com.yihua.bom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yihua.bom.Mapper.BomHeaderMapper;
import com.yihua.bom.entity.BomHeader;
import com.yihua.bom.service.IBomHeaderService;
import org.springframework.stereotype.Service;

@Service
public class BomHeaderServiceImpl extends ServiceImpl<BomHeaderMapper, BomHeader> implements IBomHeaderService {
}
