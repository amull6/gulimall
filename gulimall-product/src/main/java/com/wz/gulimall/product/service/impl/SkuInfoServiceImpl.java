package com.wz.gulimall.product.service.impl;

import com.wz.gulimall.product.entity.SkuImagesEntity;
import com.wz.gulimall.product.entity.SpuInfoDescEntity;
import com.wz.gulimall.product.service.*;
import com.wz.gulimall.product.vo.SkuItemSaleAttrVo;
import com.wz.gulimall.product.vo.SkuItemVo;
import com.wz.gulimall.product.vo.SpuItemGroupAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.product.dao.SkuInfoDao;
import com.wz.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    AttrGroupService attrGroupService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((obj) -> {
                obj.eq("sku_id", key).or().eq("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !catelogId.equalsIgnoreCase("0")) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !brandId.equalsIgnoreCase("0")) {
            queryWrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(min)) {
            try {
                BigDecimal maxDec = new BigDecimal(max);
                if (maxDec.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
//        1.sku基本信息  pms_sku_info
        SkuInfoEntity skuInfoEntity = this.baseMapper.selectById(skuId);
        Long spuId = skuInfoEntity.getSpuId();
        Long catalogId = skuInfoEntity.getCatalogId();
        skuItemVo.setSkuInfoEntity(skuInfoEntity);
//        2.sku图片信息  pms_sku_images
        List<SkuImagesEntity> skuImages = skuImagesService.getSkuImagesBySkuId(skuId);
        skuItemVo.setImages(skuImages);
//        3sku的销售组合信息  pms_sku_sale_attr_value
        List<SkuItemSaleAttrVo> skuItemSaleAttrVos = skuSaleAttrValueService.getSkuItemSaleAttrVo(spuId);
        skuItemVo.setSaleAttr(skuItemSaleAttrVos);
//        4.spu介绍 pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getBySpuId(spuId);
        skuItemVo.setDesp(spuInfoDescEntity);
//        5.获取spu规格参数信息 pms_attr_group pms_product_attr_value
        List<SpuItemGroupAttrVo> spuItemGroupAttrVos = attrGroupService.getSpuItemGroupAttrVo(spuId, catalogId);
        skuItemVo.setGroupAttr(spuItemGroupAttrVos);
        return skuItemVo;
    }

}