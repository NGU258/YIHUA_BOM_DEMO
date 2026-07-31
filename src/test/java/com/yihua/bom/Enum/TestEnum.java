package com.yihua.bom.Enum;

import com.yihua.bom.constants.Enum.bom.BomType;
import com.yihua.bom.constants.Enum.material.MaterialType;
import com.yihua.bom.constants.constant.BOMConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestEnum {
    @Test
    public void TestEnumCall(){
        System.out.println(BomType.EBOM);//这里直接输出调用的是toString 打印的是那个枚举常量名 比如有个定义是A("a") 则toString打印的是A getValue打印的是a
        System.out.println(BomType.EBOM.getValue());

        System.out.println(MaterialType.PRODUCT); //PRODUCT 这个是返回的枚举对象 并不是字符串
        System.out.println(MaterialType.PRODUCT.equals("product"));//false 枚举对象的equals判断逻辑是两边是不是同一个枚举实例 而这里看到是String自然就返回了false
        System.out.println(MaterialType.PRODUCT.equals("PRODUCT"));//false
        System.out.println(MaterialType.PRODUCT.getValue().equals("PRODUCT")); //true 这里的getValue才会返回String 所以就判成功了
        System.out.println(MaterialType.PRODUCT.getValue().equals("PRODUCT")); //true

        System.out.println(MaterialType.PRODUCT.getDesc());
        System.out.println(MaterialType.PRODUCT.getValue());

    }



    @Test
    public void TestConstantsCall(){
        System.out.println(BOMConstants.BOM_TYPE_MBOM);//这里调用就会把里面的所有常量名都显示出来 就显得有点冗余了
    }
}
