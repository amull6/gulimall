package com.wz.gulimall.product.vo;

import com.wz.gulimall.product.entity.SkuImagesEntity;
import com.wz.gulimall.product.entity.SkuInfoEntity;
import com.wz.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SkuItemVo {
    //1.sku基本信息  pms_sku_info
    SkuInfoEntity skuInfoEntity;
    //2.sku图片信息  pms_sku_images
    List<SkuImagesEntity> images;
    //3sku的销售组合信息  pms_sku_sale_attr_value
    List<SkuItemSaleAttrVo> saleAttr;
    //4.spu介绍 pms_spu_info_desc
    SpuInfoDescEntity desp;
    //5.获取spu规格参数信息 pms_attr_group pms_product_attr_value
    List<SpuItemGroupAttrVo> groupAttr;
}
