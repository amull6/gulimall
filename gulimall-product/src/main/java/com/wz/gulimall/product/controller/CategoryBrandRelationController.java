package com.wz.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wz.gulimall.product.entity.BrandEntity;
import com.wz.gulimall.product.service.BrandService;
import com.wz.gulimall.product.vo.BrandRespVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wz.gulimall.product.entity.CategoryBrandRelationEntity;
import com.wz.gulimall.product.service.CategoryBrandRelationService;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.R;


/**
 * 品牌分类关联
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @GetMapping("/brands/list")
    public R bandListByCatId(@RequestParam("catId") Long catId) {
        List<BrandEntity> brandEntities = categoryBrandRelationService.listBrandEntities(catId);List<BrandRespVo> brandRespVos = brandEntities.stream().map((obj) -> {
            BrandRespVo brandRespVo = new BrandRespVo();
            brandRespVo.setBrandId(obj.getBrandId());
            brandRespVo.setBrandName(obj.getName());
            return brandRespVo;
        }).collect(Collectors.toList());
        return R.ok().put("data", brandRespVos);
    }


    /**
     * 列表
     */
    @RequestMapping("/catelog/list")
    public R catelogList(@RequestParam("brandId") long brandId) {
        List<CategoryBrandRelationEntity> list = categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
        return R.ok().put("data", list);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {

        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
