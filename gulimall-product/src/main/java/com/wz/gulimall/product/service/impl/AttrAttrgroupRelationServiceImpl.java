package com.wz.gulimall.product.service.impl;

import com.wz.gulimall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.wz.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wz.gulimall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveRelationBatch(List<AttrGroupRelationVo> vos) {
        List<AttrAttrgroupRelationEntity>  attrgroupRelationEntities=vos.stream().map((i)->{
            AttrAttrgroupRelationEntity attrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                BeanUtils.copyProperties(i,attrgroupRelationEntity);
            return attrgroupRelationEntity;
        }).collect(Collectors.toList());
        this.saveBatch(attrgroupRelationEntities);

    }

}