package com.wz.gulimall.search.vo;

import com.wz.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {
    //    商品信息
    private List<SkuEsModel> products;
    //    分页信息
    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    private List<Integer> pageNavs;

    List<BrandVo> brands;
    List<AttrVo> attrs;//涉及属性
    List<CatalogVo> catalogs;//涉及分类

    /* 面包屑导航数据 */
    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }


    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }

    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

}
