package com.wz.gulimall.product.service.impl;


import com.alibaba.fastjson.TypeReference;
import com.wz.common.constant.ProductConstant;
import com.wz.common.to.SkuHasStockVo;
import com.wz.common.to.SkuReductionTo;
import com.wz.common.to.SpuBoundTo;
import com.wz.common.to.es.SkuEsModel;
import com.wz.common.utils.R;
import com.wz.gulimall.product.entity.*;
import com.wz.gulimall.product.feign.CouponFeignService;
import com.wz.gulimall.product.feign.SearchFeighService;
import com.wz.gulimall.product.feign.WareFeignService;
import com.wz.gulimall.product.service.*;
import com.wz.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.product.dao.SpuInfoDao;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService imagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeighService searchFeighService;



    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
//        spu基本信息保存
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);
//保存商品描述信息
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
//图片集
        List<String> images = vo.getImages();
        imagesService.saveImages(spuInfoEntity.getId(), images);
//        spu规格参数
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map((obj) -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            AttrEntity attrEntity = attrService.getById(obj.getAttrId());
            productAttrValueEntity.setAttrId(obj.getAttrId());
            productAttrValueEntity.setAttrValue(obj.getAttrValues());
            productAttrValueEntity.setQuickShow(obj.getShowDesc());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveBatch(productAttrValueEntities);
//        保存spu积分信息
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        Bounds bounds = vo.getBounds();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存Sku积分信息失败");
        }

//        保存sku信息
        List<Skus> skusList = vo.getSkus();
        if (skusList != null && skusList.size() > 0) {
            skusList.forEach((item) -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.save(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();
//                保存sku图片
                List<SkuImagesEntity> skuImagesEntities = item.getImages().stream().map((obj) -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                    skuImagesEntity.setImgUrl(obj.getImgUrl());
                    skuImagesEntity.setDefaultImg(obj.getDefaultImg());
                    return skuImagesEntity;
                }).filter((obj) -> {
                    return !StringUtils.isEmpty(obj.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

//                保存销售属性
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = item.getAttr().stream().map((obj) -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(obj, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

//                优惠满减信息
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                if (item.getFullCount() > 0 || item.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    BeanUtils.copyProperties(item, skuReductionTo);
                    skuReductionTo.setSkuId(skuId);
                    R reductionR = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (reductionR.getCode() != 0) {
                        log.error("远程保存Sku优惠信息失败");
                    }
                }
            });
        }


    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        // status=1 and (id=1 or spu_name like xxx)
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        /**
         * status: 2
         * key:
         * brandId: 9
         * catelogId: 225
         */

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
//        根据spuId查询所有sku
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
//        集中调用库存服务查询库存状态
        Map<Long, Boolean> hasStockMap = null;
        try {
            R skuHasStockVos = wareFeignService.hasStock(skuIds);
//        封装map
            hasStockMap = skuHasStockVos.getData(new TypeReference<List<SkuHasStockVo>>() {}).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId,SkuHasStockVo::getHasStock ));
        } catch (Exception e) {
            log.error("远程获取库存状态失败，{}",e);
        }
//        根据spuId查询可用来检索的规格属性组成List
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
//        查找list中可检索AttrId
        List<Long> attrIds = productAttrValueEntities.stream().map((obj) -> {
            return obj.getAttrId();
        }).collect(Collectors.toList());
//        根据IDs查找可检索IDs
        List<Long> searchAttIds = attrService.searchAttIds(attrIds);
//        转set集合
        Set<Long> searchAttIdset = new HashSet<>(searchAttIds);
        List<SkuEsModel.Attr> attrs = productAttrValueEntities.stream().filter((obj) -> {
                    return searchAttIdset.contains(obj.getAttrId());
                }
        ).map((obj) -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(obj, attr);
            return attr;
        }).collect(Collectors.toList());
//        筛选sku将符合条件的sku转成Es商品对象
        Map<Long, Boolean> finalHasStockMap = hasStockMap;
        List<SkuEsModel> skuEsModels = skuInfoEntities.stream().map((sku) -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            skuEsModel.setHotScore(0L);
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
//           设置可搜索属性
            skuEsModel.setAttrs(attrs);
//            库存状态
            skuEsModel.setHasStock(finalHasStockMap.get(sku.getSkuId()));
            return skuEsModel;
        }).collect(Collectors.toList());

//        商品上架传输到Es
        R r = searchFeighService.productUp(skuEsModels);
        if (r.getCode() == 0) {
//            该状态
            baseMapper.updatePublishStatus(spuId, ProductConstant.PublishStatus.UP.getCode());
        }else{
            //TODO 远程调用 充实调用
        }
//
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long id) {
       SkuInfoEntity skuInfoEntity = skuInfoService.getById(id);
        return this.getById(skuInfoEntity.getSpuId());
    }


}