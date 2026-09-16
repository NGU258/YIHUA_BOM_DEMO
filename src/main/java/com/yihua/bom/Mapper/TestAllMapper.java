package com.yihua.bom.Mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yihua.bom.entity.FairyCat;
import com.yihua.bom.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    //最佳实践： 当方法形参有多个参数(两个及以上)时 则一律加@Param注解来指定它的名称
    //细节一：Mybatis3.4.1以上的版本(当前项目3.5.15)都默认支持使用实际参数名称(useActualParamName = true)
    //细节二：Mysql的JDBC默认返回的是"匹配的行数" 而不是"原字段值有变化所处的那几行/受影响的行数" 因为原字段值是1我仍更新成了1这里值没变并不受影响所以应该算成匹配的行数 而不是受影响的行数 所以可以通过这个细节来判断更新了几行(返回n,n是匹配的行数) 跟有没有更新成功(更新失败返回0) 所以这里的返回值void优化成int会更好
    int updateMaterialName(@Param("materialCode")String materialCode,@Param(("materialName")) String materialName); //通过物料编号更新一下它的物料名

    //测试一下sql代码片段
    List<Material> getMaterialAllData();
}
