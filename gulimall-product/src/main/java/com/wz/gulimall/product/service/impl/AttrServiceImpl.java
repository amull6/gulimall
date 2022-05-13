package com.wz.gulimall.product.service.impl;

import com.wz.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wz.gulimall.product.entity.AttrGroupEntity;
import com.wz.gulimall.product.entity.CategoryEntity;
import com.wz.gulimall.product.service.AttrAttrgroupRelationService;
import com.wz.gulimall.product.service.AttrGroupService;
import com.wz.gulimall.product.service.CategoryService;
import com.wz.gulimall.product.vo.AttrRespVo;
import com.wz.gulimall.product.vo.AttrVo;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.product.dao.AttrDao;
import com.wz.gulimall.product.entity.AttrEntity;
import com.wz.gulimall.product.service.AttrService;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService relationService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrGroupService attrGroupService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVo attrvo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrvo, attrEntity);
        this.save(attrEntity);
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrId(attrEntity.getAttrId());
        relationEntity.setAttrGroupId(attrvo.getAttrGroupId());
        relationService.save(relationEntity);
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, long catelogId) {
//        查出原Po分页信息
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>();
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((i) -> {
                i.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);
//        循环遍历处理组成新Vo信息
        List<AttrEntity> attrEntities = page.getRecords();
        List<AttrRespVo> attrRespVos = attrEntities.stream().map((item) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(item, attrRespVo);
//        查找分类名称
            CategoryEntity categoryEntity = categoryService.getById(item.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
//        查找分组名称
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", item.getAttrId()));
            if (attrAttrgroupRelationEntity != null) {
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrAttrgroupRelationEntity.getAttrGroupId());
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(attrRespVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
//        查询AttrPO
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);
//        查询分组ID
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
        if (attrAttrgroupRelationEntity != null) {
            attrRespVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
            AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrRespVo.getAttrGroupId());
            if (attrGroupEntity != null) {
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }
//        查询分类名称
        CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
        if (categoryEntity != null) {
            attrRespVo.setCatelogName(categoryEntity.getName());
            //        查询CatelogPath
            Long[] catelogPath = categoryService.getCatelogPath(attrEntity.getCatelogId());
            attrRespVo.setCatelogPath(catelogPath);
        }
        return attrRespVo;
    }

    @Override
    public void updateAttrInfo(AttrVo attrVo) {
//        修改AttrPO
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);
//        修改AttrGroupRelationEntity
        if (attrVo.getAttrGroupId() == null) {
            relationService.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
        } else {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrVo.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            int count = relationService.count(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            if (count > 0) {
                relationService.update(attrAttrgroupRelationEntity, new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            } else {
                relationService.save(attrAttrgroupRelationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(long attrgroupId) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> ids = attrAttrgroupRelationEntities.stream().map((attrAttrgroupRelationEntity) -> {
            return attrAttrgroupRelationEntity.getAttrId();
        }).collect(Collectors.toList());
        return ids.size() > 0 ? (List<AttrEntity>) this.listByIds(ids) : null;
    }
}