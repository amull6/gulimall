package com.wz.gulimall.product.service.impl;

import com.wz.common.constant.ProductConstant;
import com.wz.gulimall.product.entity.*;
import com.wz.gulimall.product.service.*;
import com.wz.gulimall.product.vo.AttrRespVo;
import com.wz.gulimall.product.vo.AttrVo;
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
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService relationService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    ProductAttrValueService productAttrValueService;

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
        if (attrvo.getAttrGroupId() != null && attrvo.getAttrType() != ProductConstant.AttrType.ATTR_TYPE_SALE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attrvo.getAttrGroupId());
            relationService.save(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, long catelogId, String type) {
//        查出原Po分页信息
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", type.equals("base") ? ProductConstant.AttrType.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrType.ATTR_TYPE_SALE.getCode());
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
            if (item.getCatelogId() != null) {
                //        查找分类名称
                CategoryEntity categoryEntity = categoryService.getById(item.getCatelogId());
                if (categoryEntity != null) {
                    attrRespVo.setCatelogName(categoryEntity.getName());
                }
            }

//        查找分组名称
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", item.getAttrId()));
            if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
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
        if (attrEntity.getAttrType() == ProductConstant.AttrType.ATTR_TYPE_BASE.getCode()) {
            //        查询分组ID
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (attrAttrgroupRelationEntity != null) {
                attrRespVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrRespVo.getAttrGroupId());
                if (attrGroupEntity != null) {
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
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
        if (attrEntity.getAttrType() == ProductConstant.AttrType.ATTR_TYPE_BASE.getCode()) {
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
    }

    @Override
    public List<AttrEntity> getRelationAttr(long attrgroupId) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> ids = attrAttrgroupRelationEntities.stream().map((attrAttrgroupRelationEntity) -> {
            return attrAttrgroupRelationEntity.getAttrId();
        }).collect(Collectors.toList());
        return ids.size() > 0 ? (List<AttrEntity>) this.listByIds(ids) : null;
    }

    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, long attrgroupId) {
//        获取分组
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrgroupId);
//        根据分组Id获取分类信息
        Long catergorId = attrGroupEntity.getCatelogId();
//        根据分类Id查找所有分组
        List<AttrGroupEntity> attrGroupEntities = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catergorId));
//        遍历组成分组ID集合
        List<Long> groupIds = attrGroupEntities.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
//        从属性分组关系表中查找所有在分组ID集合中的数据
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
//        遍历组成属性ID集合
        List<Long> attrIds = attrAttrgroupRelationEntities.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
//        查找分类Id下所有关联属性且不在属性ID集合中的属性且属性类型不属于销售属性
//        分页及查询条件处理
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((i) -> {
                i.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        if (attrIds != null && attrIds.size() > 0) {
            queryWrapper.notIn("attr_id", attrIds);
        }
        queryWrapper.eq("catelog_id", catergorId).ne("attr_type", ProductConstant.AttrType.ATTR_TYPE_SALE);
        IPage page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<ProductAttrValueEntity> listforSpu(Long spuId) {
        return productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
    }

    @Override
    public void updateAttr(Long spuId, List<ProductAttrValueEntity> productAttrValueEntities) {
        productAttrValueService.remove(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        List<ProductAttrValueEntity> productAttrValueEntities1 = productAttrValueEntities.stream().map((item) -> {
            item.setSpuId(spuId);
            return item;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities1);
    }

    @Override
    public List<Long> searchAttIds(List<Long> attrIds) {
        List<Long> searchAttrIds = baseMapper.searchAttIds(attrIds);
        return searchAttrIds;
    }
}