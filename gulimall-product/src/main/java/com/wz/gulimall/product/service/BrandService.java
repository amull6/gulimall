package com.wz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wz.common.utils.PageUtils;
import com.wz.gulimall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);

    List<BrandEntity> getByIds(List<Long> brandIds);
}

