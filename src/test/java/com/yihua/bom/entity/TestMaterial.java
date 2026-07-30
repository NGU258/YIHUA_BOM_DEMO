package com.yihua.bom.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestMaterial {
    @Test
    public void testMaterialBuilder(){

        Material m1 = Material.builder()
                .materialCode("fairyCa001")
                .materialName("成品A")
                .materialType("标准")
                .build();

        System.out.println(m1.toString());
    }
}
