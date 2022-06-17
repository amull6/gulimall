package com.wz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wz.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wz.gulimall.product.entity.AttrEntity;
import com.wz.gulimall.product.service.AttrService;
import com.wz.gulimall.product.vo.AttrGroupRelationVo;
import com.wz.gulimall.product.vo.AttrGroupWithAttrVo;
import com.wz.gulimall.product.vo.SpuItemGroupAttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.product.dao.AttrGroupDao;
import com.wz.gulimall.product.entity.AttrGroupEntity;
import com.wz.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, long catId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((i) -> {
                i.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        IPage<AttrGroupEntity> page = new Page<>();
        if (catId == 0) {
            page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper
            );
        } else {
            queryWrapper.eq("catelog_id",catId);
            page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper
            );
        }
        return new PageUtils(page);
    }

    @Override
    public void deleteRelations(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = Arrays.asList(vos).stream().map((vo)->{
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
//            BeanUtils.copyProperties(vos,entity);
            entity.setAttrGroupId(vo.getAttrGroupId());
            entity.setAttrId(vo.getAttrId());
            return entity;
        }).collect(Collectors.toList());
        baseMapper.removeRelations(attrAttrgroupRelationEntities);

    }

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttrByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrVo> attrGroupWithAttrVos = attrGroupEntities.stream().map((obj) -> {
            AttrGroupWithAttrVo attrGroupWithAttrVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(obj, attrGroupWithAttrVo);
            List<AttrEntity> attrEntities = attrService.getRelationAttr(obj.getAttrGroupId());
            attrGroupWithAttrVo.setAttrs(attrEntities);
            return attrGroupWithAttrVo;
        }).collect(Collectors.toList());
        return attrGroupWithAttrVos;
    }

    @Override
    public List<SpuItemGroupAttrVo> getSpuItemGroupAttrVo(Long spuId, Long catalogId) {
        return this.baseMapper.getSpuItemGroupAttrVo(spuId,catalogId);
    }
}