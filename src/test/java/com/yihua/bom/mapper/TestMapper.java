package com.yihua.bom.mapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.yihua.bom.Mapper.TestAllMapper;
import com.yihua.bom.entity.Material;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
public class TestMapper {

    TestAllMapper testAllMapper;
    public TestMapper(@Autowired TestAllMapper testAllMapper){
        this.testAllMapper = testAllMapper;
    }

    //测试一下自定义Mapper的xml中返回值是Map的用法
    @Test
    public void testMaterialMapper(){
        System.out.println("\n————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————");
        List<Map> $超级炸弹喵$ = testAllMapper.getMaterialMapList();
        log.info(JSON.toJSONString($超级炸弹喵$));
        log.info(JSONArray.toJSONString($超级炸弹喵$));
        //这里测试发现了一个细节
        //打印的JSON数据如下： [{"material_type":"raw_material","unit":"斤","update_time":"2026-08-08T10:52:53","deleted":false,"create_time":"2026-08-08T10:52:53","id":8,"material_name":"机箱炸弹","spec":"Testgc","enabled":true,"material_code":"fairyCat008"}]
        //细节一： 其中update_time在数据库中存的是2026-08-08 10:52:53 但打印出来的时候是2026-08-08T10:52:53 也就是时间字段中的空格会自动转换成T
        //细节二： 其中deleted在数据库中存的是0 但打印出来的时候是false 也就是说它会自动进行转换
        //根据这个细节再判断一下它的布尔值
        $超级炸弹喵$.forEach(cur-> System.out.println("enabled:"+cur.get("enabled")));
        System.out.println("————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————\n");
  }

  //测试一下类型别名
    @Test
    public void testTypeAlias(){
        System.out.println(testAllMapper.getFirstMaterialName());
    }

    //测试一下动态sql的用法
    @Test
    public void testDynamicSql(){
        System.out.println(testAllMapper.getMaterialByMaterialType(Arrays.asList("SEMI_FINISHED","RAW_MATERIAL")));;
    }

    //测试一下sql版本的switch： choose when otherwise
    @Test
    public void testSqlVersionSwitch(){
        System.out.println(testAllMapper.getMaterialInfoByMaterialType("semi_finished1"));
    }

    //复习并测试一下<update>标签的用法
    @Test
    public void testUpdateLabel(){
        testAllMapper.updateMateiralName("fairyCat001","元气小喵仙~");
    }

    //测试sql代码片段
    @Test
    public void testSqlFrag(){
        System.out.println(testAllMapper.getMaterialAllData());
    }

}
