package com.wz.gulimall.search.controller;

import com.wz.common.exception.BizCodeEnum;
import com.wz.common.to.SkuHasStockVo;
import com.wz.common.to.es.SkuEsModel;
import com.wz.common.utils.R;
import com.wz.gulimall.search.service.ProductUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.UsesSunHttpServer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search")
@Slf4j
public class EsSaveController {
    @Autowired
    ProductUpService productUpService;
    @PostMapping("/productUp")
    public R productUp(@RequestBody List<SkuEsModel> skuEsModelList){
        R r = null;
        boolean hasFailuress = false;
        try {
            hasFailuress = productUpService.productUp(skuEsModelList);
        } catch (IOException e) {
            log.error("商品上架异常{}",e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (hasFailuress) {
            r = R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }else{
            r = R.ok();
        }
        return r;
    }
}
