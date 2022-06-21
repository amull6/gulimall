package com.wz.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wz.gulimall.product.entity.BrandEntity;
import com.wz.gulimall.product.service.BrandService;
import com.wz.gulimall.product.service.SkuInfoService;
import com.wz.gulimall.product.vo.SkuItemVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.ExecutionException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Autowired
    SkuInfoService skuInfoService;

    @Test
    public void testItem() throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = skuInfoService.item(7L);
        System.out.println(skuItemVo);
    }
    @Test
    public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("hello");
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功");
    }

    @Test
    public void testUpdate() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setDescript("helloworld");
//        brandEntity.setName("华为");
//        brandEntity.setBrandId(1L);
//        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("name", "华为"));
//        list.forEach((be)->{
//            System.out.println(be);
//        });
    }

}
