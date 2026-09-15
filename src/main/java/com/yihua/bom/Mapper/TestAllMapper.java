package com.yihua.bom.Mapper;

import com.yihua.bom.entity.FairyCat;
import com.yihua.bom.entity.Material;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TestAllMapper {

    List<Map> getMaterialMapList();

    String getFirstMaterialName();

    //查询物料类型相关的记录
    List<Map> getMaterialByMaterialType(List<String> materialType);

    //测试一下sql版本的switch： choose when otherwise
    List<Map> getMaterialInfoByMaterialType(String materialType);

    //复习并测试一下<update>标签的用法
    void updateMateiralName(String materialCode,String materialName); //通过物料编号更新一下它的物料名

    //测试一下sql代码片段
    List<Material> getMaterialAllData();
}
