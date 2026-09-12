package com.yihua.bom.Mapper;

import com.yihua.bom.entity.FairyCat;
import com.yihua.bom.entity.Material;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TestAllMapper {

    List<Map> getMaterialMapList();
}
