package com.wz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wz.common.utils.PageUtils;
import com.wz.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wz.gulimall.product.vo.AttrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveRelationBatch(List<AttrGroupRelationVo> vos);
}

