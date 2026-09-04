package com.yihua.bom.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//第二步中的逻辑  2.1 给类加个注解@ExcelSheet将它跟Excel表进行绑定
//zh表示中文环境下的名字 en表示英文环境下的名字 在切换成不同的语言环境时会显示其不同的语言
@ExcelSheet(zh = "元气小喵仙" , en ="Energetic Little Cat Fairy")
public class FairyCatExcel {

    //2.2给类的属性名添加 @ExcelColumn 将它跟Excel表中的字段列进行绑定
    //如果有多个属性名 就在这些属性名的前面加@ExcelColumn注解就行了
    //导出列并不只是指数据库表字段，而是那些被导出注解标记过的字段
    //意味着它可以是任意字段，只要它身上有这个注解 它就能当导出列
    @ExcelColumn(zh = "性别" , en = "Sex")
    private String sex;

    @ExcelColumn(zh = "名字" , en = "Name")
    private String name;

}
