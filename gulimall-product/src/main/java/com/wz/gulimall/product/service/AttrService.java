package com.wz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wz.common.utils.PageUtils;
import com.wz.gulimall.product.entity.AttrEntity;
import com.wz.gulimall.product.vo.AttrRespVo;
import com.wz.gulimall.product.vo.AttrVo;

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

    PageUtils queryBaseAttrPage(Map<String, Object> params, long catelogId);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttrInfo(AttrVo attrVo);
}

