package com.wz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wz.common.utils.PageUtils;
import com.wz.gulimall.product.entity.ProductAttrValueEntity;

import java.util.Map;

/**
 * spu属性值
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

