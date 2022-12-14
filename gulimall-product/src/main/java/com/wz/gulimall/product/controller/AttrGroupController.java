package com.wz.gulimall.product.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.wz.gulimall.product.entity.AttrEntity;
import com.wz.gulimall.product.service.AttrAttrgroupRelationService;
import com.wz.gulimall.product.service.AttrService;
import com.wz.gulimall.product.service.CategoryService;
import com.wz.gulimall.product.service.impl.AttrAttrgroupRelationServiceImpl;
import com.wz.gulimall.product.vo.AttrGroupRelationVo;
import com.wz.gulimall.product.vo.AttrGroupWithAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wz.gulimall.product.entity.AttrGroupEntity;
import com.wz.gulimall.product.service.AttrGroupService;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.R;


/**
 * 属性分组
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttr(@PathVariable Long catelogId) {
        List<AttrGroupWithAttrVo> attrGroupWithAttrVos = attrGroupService.getAttrGroupWithAttrByCatelogId(catelogId);
        return R.ok().put("data", attrGroupWithAttrVos);
    }


    @PostMapping("/attr/relation")
    public R saveRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        attrAttrgroupRelationService.saveRelationBatch(vos);
        return R.ok();
    }

    @GetMapping("/{attrgroupId}/noattr/relation")
    public R getNoRelationAttr(@RequestParam Map<String, Object> params, @PathVariable long attrgroupId) {
        PageUtils pageUtils = attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/attr/relation/delete")
    public R deleteRelations(@RequestBody AttrGroupRelationVo[] vos) {
        attrGroupService.deleteRelations(vos);
        return R.ok();
    }

    @GetMapping("/{attrgroupId}/attr/relation")
    public R getRelationAttr(@PathVariable long attrgroupId) {
        List<AttrEntity> attrEntityList = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", attrEntityList);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable long catId) {
        PageUtils page = attrGroupService.queryPage(params, catId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        attrGroup.setCatelogPath(categoryService.getCatelogPath(attrGroup.getCatelogId()));
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
