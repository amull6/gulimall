package com.wz.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wz.gulimall.product.entity.BrandEntity;
import com.wz.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("hello");
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功");

    }

    @Test
    void testUpdate() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("helloworld");
        brandEntity.setName("华为");
        brandEntity.setBrandId(1L);
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("name", "华为"));
        list.forEach((be)->{
            System.out.println(be);
        });

    }

}
