package com.wz.gulimall.product.service.impl;

import com.wz.gulimall.product.vo.SpuSaveVo;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.product.dao.SkuInfoDao;
import com.wz.gulimall.product.entity.SkuInfoEntity;
import com.wz.gulimall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
//        4. 基本信息  `pms_spu_info`
//        SpuInfo
//        5. 描述图片 `pms_spu_info_des` 介绍描述
//        6. 图片集 `pms_spu_images`
//        7. spu规格参数`pms_product_attr_value`
//        8. spu积分信息 gulimall-sms-》sms_spu_bounds
//        9. spu对应的sku信息
//        1. 基本信息 `pms_sku_info`
//        2. sku描述图片
//        3. sku销售属性
//        4. sku的优惠信息 gulimall-sms-》sms_sku_ladder 打折\sms_sku_full_reduce\sms_member_price

    }

}