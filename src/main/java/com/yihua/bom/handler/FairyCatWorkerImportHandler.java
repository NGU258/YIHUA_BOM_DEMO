package com.yihua.bom.handler;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yihua.bom.entity.FairyCat;
import com.yihua.bom.entity.FairyCatExcel;
import com.yihua.bom.service.IMaterialService;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

//模板导入类使用方法

// 1. 使用导入注解 ImportService  里面的属性如下：
// templateCode是模板编码 必填
//       模板编码命名规范： 服务简写.表名 全大写  示例 LMDS.CATEGORY 方便老师快速知晓它导入的是啥内容
// tenantNum是模板编码  一般不会填它
// sheetIndex是Excel中每个sheet表的索引 默认从0开始  这里也就意味着一个导入处理类对应一个sheet
// sheetName是设置表名的属性
@ImportService(templateCode = "LMDS.TABLENAME") //案例在hlos-mds中的WorkerImportService实现类中
public class FairyCatWorkerImportHandler extends BatchImportHandler { //2. 接着继承批量导入功能的抽象类BatchImportHandler

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private IMaterialService materialService;

    //3. 重写doImport跟getSize方法
    @Override
    public Boolean doImport(List<String> data) {
        //4. Excel中的数据会传入到这个data里面 它是一个JSON字符串  所以需要反序列化成JAVA对象 后面执行相应的逻辑即可
        if(CollectionUtils.isEmpty(data))
            return true;

        //获取自定义参数 可以通过调用getArgs方法来获取
        Map<String, Object> args = getArgs();

        List<FairyCatExcel> updateList = new ArrayList<>();
        List<FairyCatExcel> insertList = new ArrayList<>();

        //通过调用objectMapper对象里面工厂类TypeFactory中的公共方法constructParametricType构造出 ArrayList<CategorySet> 这个参数化类型
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, FairyCatExcel.class);

        List<FairyCatExcel> fairyCatExcelList ;

        try {
            //接着将data中的所有Excel行数据通过传入的参数化类型ArrayList<CategorySet>反序列化成List<FairyCatExcel>
            fairyCatExcelList = objectMapper.readValue(data.toString(),javaType); //反序列化方法objectMapper.readValue(要被序列化的字符串，反序列化后的最终类型)
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        //关于流式写法
        //流式写法的核心是流式管道结构，它由一个数据源（如集合）、零个或多个中间操作（如 filter、map）以及一个终止操作（如 forEach或collect）组成。
        //示例： 数据源.stream() → 0~N 个中间操作（可跳过） → 1 个终止操作
        //中间操作如： filter（筛选,丢弃不符合条件的行）、map（转换）、distinct（去重）、sorted（排序）根据自己的需求选择
        //流式写法的优点是代码简洁、易读、易维护，并且可以避免频繁的循环遍历。

        //通过name进行分组 这个Map前面的key是那个分类字段name  后面的value是分类好后的记录列表
        Map<String, List<FairyCatExcel>> groupList = fairyCatExcelList.stream().collect(Collectors.groupingBy(FairyCatExcel::getName));
        //等价于下面这个传统写法
        Map<String,List<FairyCatExcel>> map =new HashMap<>();
        for(FairyCatExcel cur: fairyCatExcelList){
            //方法逻辑跟默认值的逻辑差不多  它还用到了懒加载的思维
            map.computeIfAbsent(cur.getName(),k->new ArrayList<>()).add(cur); //computeIfAbsent(key,Lambda表达式)方法的逻辑是 如果key存在 就用内部的List调用add  如果key不存在 就调用Lambda表达式新建一个数组 然后将元素add进来
        }

        //判重逻辑 写法一
        for(Map.Entry<String,List<FairyCatExcel>> entry: groupList.entrySet()){

            //对Excel中的所有行数据进行判重 如果名字对应的行数出现了两次及以上 就是有重复的行出现
            if(entry.getValue().size()>1){

                //getContextList是框架提供的当前导入的所有原始行（data 里每个 JSON 加上错误信息封装成的行对象）要定位是哪几行出错 就得去原始行里面找 它是抽象类BatchImportHandler中的方法
                getContextList().stream().filter(d->{ //把原始行数据中重复出现的那些行给过滤掉
                    try {
                        //过滤掉这些名字重复出现的行
                        //细节： 因为原始行是一个JSON  所以需要反序列化成FairyCatExcel对象  d代表每个原始行记录
                        return entry.getKey().equals(objectMapper.readValue(d.getData(),FairyCatExcel.class).getName());
                    }catch(IOException e){
//                        e.printStackTrace();
                         return false;
                    }
                }).forEach(e->e.addErrorMsg("性别有丢丢重复")); //给这几行打上错误标记。前端展示的时候在Excel中这些错误行就会标红提示这句话，用户一眼就能看出哪几行错了
                return false;
            }
        }
        //判重逻辑 写法二
        groupList.forEach((key,value)->{
             if(value.size()>1){
                 getContextList().stream().filter(cur->{
                    try{
                        return key.equals(objectMapper.readValue(cur.getData(),FairyCatExcel.class).getName());
                    }catch(Exception exception){
                        exception.printStackTrace();
                        return false;
                    }
                 }).forEach(e->e.addErrorMsg("姓名出现重复"));
             }
        });
        //接着拿它里面的key集合
        Set<String> groupKeys = groupList.keySet();

        //先把符合条件的数据从数据库中查出来

        //接着遍历每个导入的行数据 判断当前这条行数据的某个字段在数据库中存不存在
        //如果存在的话 就将元素放到更新数组updateList中 这里记得要给它传主键 因为Excel的行数据中所在的主键值可能是空的
        //如果不存在的话 就放到插入数组insertList中

        //最后再执行批量插入或更新操作即可
        return true;

        //参照hlos-mds项目中的WorkerImportService类里面的doImport方法后面的逻辑 以下是自己的理解
        //获取租户id的方式
        //  方法一： 用TenantLimitedHelper.tenantId()方法  查库时用它
                //它本质读的是当前线程上下文（ThreadLocal）里绑定的租户ID 由环境决定 可以被切换
        //  方法二： DetailsHelper.getUserDetails().getTenantId() 新增数据给实体类字段填充字段值时就用它
                //DetailsHelper 取的是当前登录用户的安全上下文信息(从token解析出来的用户信息 UserDetails)

        //CategorySet cs = dbCategorySets.stream().filter(c->categorySet.getCategorySetCode().equals(c.getCategorySetCode())).findFirst().orElse(null);
        //.findFirst()  表示取出第一条符合条件的元素
        //.orElse(null) 表示找不到就返回null 而不是抛出异常

        //categorySetRepository.batchUpdateByPrimaryKeySelective(updateList);
        //batchUpdateByPrimaryKeySelective中的 Selective就代表着更新非 null 字段（也就意味着null不会覆盖掉原值）

    }

    @Override
    public int getSize() {
        //默认返回0，也就是不会分批，一次性获取所有数据
        //用于每次取ExcelSheet表中的行数据 如果总行数是130行 getSize是返回60行 则第一次取60 第二次取60 第三次就会取10
        return 60;
    }
}
