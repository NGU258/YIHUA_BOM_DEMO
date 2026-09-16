package com.yihua.bom.mapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yihua.bom.Mapper.MaterialMapper;
import com.yihua.bom.Mapper.TestAllMapper;
import com.yihua.bom.entity.Material;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
public class TestMapper {

    TestAllMapper testAllMapper;
    MaterialMapper materialMapper;
    public TestMapper(@Autowired TestAllMapper testAllMapper,@Autowired MaterialMapper materialMapper){
        this.testAllMapper = testAllMapper;
        this.materialMapper = materialMapper;
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
        int result = testAllMapper.updateMaterialName("fairyCat001","元气小喵仙~");
        if(result==0)
            System.out.println("更新失败");
        else System.out.println("更新成功，匹配的行数： "+result);
    }

    //测试sql代码片段
    @Test
    public void testSqlFrag(){
        System.out.println(testAllMapper.getMaterialAllData());
    }

    //动态Sql最快学法：看它拼成了啥 这里以更新语句updateMateiralName的四种情况为例：00 01 10 11
    //动态Sql说白了就是一个字符串拼接 类似于C中的宏
    @Test
    //这里单元测试配合事务之后竟然真的没有影响数据库 AI说这里测试结束后这个事务会自动回滚 不会污染库中的数据
    @Transactional //发现控制台中打印了这一句： transaction manager [org.springframework.jdbc.support.JdbcTransactionManager@315105f]; rollback [true] 也就是回滚成功了
    public void testDynamicSqlSpecialCase(){
       //00
        testAllMapper.updateMaterialName(null,null); //拼接出来的sql：update material SET update_time = now() WHERE 1 = 0 and deleted = 0

        //01
        testAllMapper.updateMaterialName(null,"元气小喵仙~"); //拼接出来的sql：update material SET material_name = ?, update_time = now() WHERE 1 = 0 and deleted = 0

        //10
        testAllMapper.updateMaterialName("fairyCat001",null); //拼接出来的sql：update material SET update_time = now() WHERE material_code = ? and deleted = 0

        //11
        testAllMapper.updateMaterialName("fairyCat001","元气小喵仙~"); //拼接出来的sql：update material SET material_name = ?, update_time = now() WHERE material_code = ? and deleted = 0

    }

    @Test
    public void testLimitBeforeWithSemicolon(){
        //这里current=0跟=1的效果是一样的 最终拼接出来的sql后面的limit都是limit 3 这里省略掉第一个参数就默认等同于limit 0,3 后面的这个3是size形参所接收的值 代表每页多少条数据
        //当current传2的时候 也就是第二页开始 逻辑是查第二页所在的那3条数据 这时Mybatis-plus就会自动计算 拼接的sql中limit 3就变成了limit 3,3 也就是开头第一位数字从0跳到了3 刚好是跳过了前面三行数据 0 1 2
        //然后往后 当current=3时 就是limit 6,3 也就是跳过了3 4 5
        //接着当传current=4时 就是limit 9,3 跳过了6 7 8 测试发现符合预期
        //这里不难发现 0 3 6 9是个等差数列 公差d刚好是传入的3(这里对应形参size)  根据等差数列通项公式an=a1+(n-1)d 其中d=3 a1=0 可以得出an=3n-3 所以limit的第一个参数就是通过an=3n-3的方式求出来的 这里对应代码就是假设limit的第一个参数是x 则x = current*size - size  (特殊情况：当current=0时处理逻辑跟current=1一样)
        Page<Material> page = new Page<>(4,3);
        //查一下是启用状态的所有物料
        LambdaQueryWrapper<Material> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Material::getEnabled,1);

        //先输出一下自定义Mapper中带分号的sql所执行返回的结果表
        //运行后拼接出来的Sql: select id,material_code,material_name,material_type,spec,unit,enabled,create_time,update_time,deleted from material where deleted = 0;
        testAllMapper.getMaterialAllData(); //这里我日志的级别是trace  所以我可以直接看这个sql执行后所返回查询到的数据 这里就不输出了

        //当前所拼接的Sql： SELECT id,material_code,material_name,material_type,spec,unit,enabled,create_time,update_time,deleted FROM material WHERE deleted=0 AND (enabled = ?) LIMIT ?
        //此问题待后面有时间再研究： 当自定义Mapper接口方法声明的形参跟返回值都是IPage<Material>的时候 其xml实现中写的自定义sql后面不小心加上了分号 会不会因分页拦截器拦截sql后拼接了limit而报错 会不会自动去掉limit前面的这个分号 因为理论上如果没去掉的话会报语法错误
        Page<Material> materialPage = materialMapper.selectPage(page, lqw);

        //拼接出来的Sql:select id,material_code,material_name,material_type,spec,unit,enabled,create_time,update_time,deleted from material where deleted = 0;
        testAllMapper.getMaterialAllData();

        System.out.println("序列化前： "+materialPage);

        System.out.println("调用toString(): "+materialPage.toString()); //发现直接输出这个对象数组跟调用toString的效果是一样的

        System.out.println("序列化后： "+JSON.toJSONString(materialPage, SerializerFeature.WriteMapNullValue));
    }

}
