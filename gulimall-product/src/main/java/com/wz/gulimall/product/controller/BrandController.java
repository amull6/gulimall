package com.wz.gulimall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wz.common.validator.group.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wz.gulimall.product.entity.BrandEntity;
import com.wz.gulimall.product.service.BrandService;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
        public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 信息
     */
    @RequestMapping("/infos")
    public R infos(@RequestParam("brandIds") List<Long> brandIds){
        List<BrandEntity> brands = brandService.getByIds(brandIds);
        return R.ok().put("brands", brands);
    }


    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody BrandEntity brand){
		brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand/*, BindingResult result*/){
//        if (result.hasErrors()) {
//            Map<String, String> map = new HashMap<>();
//            result.getFieldErrors().forEach((item)->{
//                map.put(item.getField(), item.getDefaultMessage());
//            });
//            return R.error(400,"提交的数据不合法").put("data",map);
//        }
        brandService.updateDetail(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
