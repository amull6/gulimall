package com.wz.gulimall.product.feign;

import com.wz.common.to.es.SkuEsModel;
import com.wz.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeighService {
    @PostMapping("/search/productUp")
    R productUp(@RequestBody List<SkuEsModel> skuEsModelList);
}
