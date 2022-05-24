package com.wz.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.wz.gulimall.product.entity.ProductAttrValueEntity;
import com.wz.gulimall.product.vo.AttrRespVo;
import com.wz.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wz.gulimall.product.entity.AttrEntity;
import com.wz.gulimall.product.service.AttrService;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.R;



/**
 * 商品属性
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @PostMapping("/update/{spuId}")
    public R updateAttr(@PathVariable Long spuId, @RequestBody List<ProductAttrValueEntity> productAttrValueEntities){
        attrService.updateAttr(spuId, productAttrValueEntities);
        return R.ok();
    }
    /**
     * 列表
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R listforSpu(@PathVariable Long spuId){
        List<ProductAttrValueEntity> productAttrValueEntities = attrService.listforSpu(spuId);
        return R.ok().put("data", productAttrValueEntities);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @GetMapping("/{type}/list/{catelogId}")
    public R baseList(@RequestParam Map<String, Object> params,@PathVariable long catelogId,@PathVariable String type){
        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,type);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{attrId}")
        public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);
        AttrRespVo attrRespVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attrRespVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody AttrVo attrvo){
		attrService.saveAttr(attrvo);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
        public R update(@RequestBody AttrVo attrVo){
		attrService.updateAttrInfo(attrVo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
