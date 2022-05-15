package com.wz.gulimall.product.service.impl;

import com.wz.gulimall.product.dao.BrandDao;
import com.wz.gulimall.product.dao.CategoryDao;
import com.wz.gulimall.product.entity.BrandEntity;
import com.wz.gulimall.product.entity.CategoryEntity;
import com.wz.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.product.dao.CategoryBrandRelationDao;
import com.wz.gulimall.product.entity.CategoryBrandRelationEntity;
import com.wz.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    CategoryDao categoryDao;
    @Autowired
    BrandDao brandDao;
    @Autowired
    BrandService brandService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        long brandId = categoryBrandRelation.getBrandId();
        long catelogId = categoryBrandRelation.getCatelogId();
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        BrandEntity brandEntity = brandDao.selectById(brandId);
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        baseMapper.insert(categoryBrandRelation);
    }

    @Override
    public void updateByBrandId(Long brandId, String brandName) {
        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        entity.setBrandId(brandId);
        entity.setBrandName(brandName);
        this.update(entity, new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void updateByCategoryId(CategoryEntity category) {
        baseMapper.updateByCategoryId(category.getCatId(), category.getName());
    }

    @Override
    public List<BrandEntity> listBrandEntities(Long catId) {
        List<CategoryBrandRelationEntity> categoryBrandRelationEntities = this.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<Long> brandIds = categoryBrandRelationEntities.stream().map((obj) -> {
            return obj.getBrandId();
        }).collect(Collectors.toList());
        List<BrandEntity> brandEntities = new ArrayList<BrandEntity>();
        if (brandIds != null && brandIds.size() > 0) {
            brandEntities = brandService.list(new QueryWrapper<BrandEntity>().in("brand_id", brandIds));
        }
        return brandEntities;
    }
}