package com.wz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wz.common.utils.PageUtils;
import com.wz.gulimall.product.entity.AttrEntity;
import com.wz.gulimall.product.entity.ProductAttrValueEntity;
import com.wz.gulimall.product.vo.AttrRespVo;
import com.wz.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attrvo);

    PageUtils queryBaseAttrPage(Map<String, Object> params, long catelogId, String type);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttrInfo(AttrVo attrVo);

    List<AttrEntity> getRelationAttr(long attrgroupId);

    PageUtils getNoRelationAttr(Map<String, Object> params, long attrgroupId);

    List<ProductAttrValueEntity> listforSpu(Long spuId);

    void updateAttr(Long spuId, List<ProductAttrValueEntity> productAttrValueEntities);
}

