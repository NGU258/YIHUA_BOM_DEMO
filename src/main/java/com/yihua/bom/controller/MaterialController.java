package com.yihua.bom.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yihua.bom.dto.MaterialDTO;
import com.yihua.bom.entity.Material;
import com.yihua.bom.service.IMaterialService;
import com.yihua.bom.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//设计： 请求方法表达CRUD 路径表达要操作的资源
//这里的设计核心理念是用请求方法表达实际的动作  用URL表达对应的资源
//这样设计的好处就是不会随着功能的增长导致爆炸式膨胀  语义上也是比较直观的
//如果全都写成get或全都写成post 然后请求路径上加个动词create或update 功能多了是比较难以维护的
//post表示insert 将数据邮寄到数据库中
//get 表示select 获取数据库中的数据
//put 表示update 将更改的数据再放回到数据库中
//delete 表示delete 这个语义就比较明确了 就直接执行对应的删除操作
@RestController
@RequiredArgsConstructor
@RequestMapping("/materials")  //在类上面指定路径的话 后面的方法就都会自动继承这个路径了
public class MaterialController {

    private final IMaterialService iMaterialService;

    //它们两个在参数校验方面的功能是一样的
    //只是validated注解比valid注解要更强一点
    //validated是Spring自己封装的 一个增强版本
    //valid是 JAVA EE的
    @PostMapping("/create")
    public Result<Material> addMaterial(@Validated @RequestBody MaterialDTO m){

          Material material = iMaterialService.createMaterial(m);

          //这里要返回Material而不是DTO对象MaterialDTO的原因是：
          // 前端需要知道插入数据后自动回填过来的主键字段id值 不然的话就没法做后续操作
          // 这里会进行自动回填主要还是@TableId注解中指定了type类型为IdType.AUTO
          // 它会告诉Mybatis-Plus 这个字段是数据库自动生成的 后面需要回填id回来 底层原理是JDBC的RETURN_GENERATE_KEYS
          // 但这里数据库字段的默认值是没法回填过来的 所以要么去实体类属性名那里再加个默认值 要么就用三目运算符判断(条件?true:false)赋值一下
          return Result.success("插入成功",material);
    }

    //这里的IPage是Mybaits-Plus分页插件里面一个专门存放分页结果的接口类 直接用就完事了
    // 这里需要传入三个参数：  页码、每页的数量、要查找的关键字
    //这里是可以发Put跟Delete请求的
    //传统的表单<form>标签只支持get跟post这两种请求
    //但现代的Web开发是用Javascript中的fetch方法发送出去的 它支持Http方法中的所有请求发送
    //示例： fetch('/materials',{ method: 'DELETE' });
    //这里浏览器只能发送get请求 得通过其它的工具(比如postman或knife4j生成的接口文档来发送对应的接口请求)
    //@RequestParam 注解的作用就是 会接收传统API写法（接口路径?name=XXX） 中name对应的字段值
    //defaultValue属性的作用就是如果前端没指定要传啥值的话则默认用当前设置的默认值
    //required属性的作用就是控制 这个字段如果没传值的话会不会报错
    //默认是true 也就是说必须得传一个值进来  不然的话就会报错
    //如果指定是false的话 则可以不必传值进来 默认值就是null 也就是直接显示所有的列表数据 并不会报错
    //如果指定了value属性值 它就会跟前端URL请求路径中的字段名进行映射  这个变量名就失效了
    //但如果没有指定value属性值的话 则前端URL请求路径中的字段名就会默认跟当前方法的形参变量名进行映射

    @GetMapping //这里不写路径的话则会自动继承上面的父路径
    public Result<IPage<Material>> listMaterial(@RequestParam(defaultValue = "1") Long pageNum,
                                              @RequestParam(defaultValue = "10")Long count,
                                              @RequestParam(value = "keyword" , required = false) String key){

        IPage<Material> listResult = iMaterialService.listMaterial(pageNum,count,key);

        return Result.success("分页查询成功！",listResult);

    }

    @GetMapping("/{materialId}")
    public Result<Material> getMaterial(@PathVariable Long materialId){
        Material material = iMaterialService.getMaterial(materialId);
        return Result.success("查询物料成功",material);
    }

    //更新成功后还是需要把这个物料信息返回一下
    @PutMapping("/{materialId}")
    public Result<Material>  updateMaterial(@PathVariable Long materialId,@RequestBody  MaterialDTO mDto){
            Material material = iMaterialService.updateMaterial(materialId,mDto);
            return Result.success("更新成功",material);
    }

    @DeleteMapping("/{materialId}")
    public Result<Material> deleteMaterial(@PathVariable Long materialId){
         Material material  = iMaterialService.deleteMaterialById(materialId);
         return Result.success("删除成功",material);
    }


}
