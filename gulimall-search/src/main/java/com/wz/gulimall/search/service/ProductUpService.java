package com.wz.gulimall.search.service;

import com.wz.common.to.es.SkuEsModel;
import com.wz.common.utils.R;

import java.io.IOException;
import java.util.List;

public interface ProductUpService {
    boolean productUp(List<SkuEsModel> skuEsModelList) throws IOException;
}
