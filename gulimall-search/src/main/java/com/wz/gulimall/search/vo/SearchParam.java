package com.wz.gulimall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    //    过滤
    private String keyword;
    private Long catalog3Id;
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> Attrs;
    //    排序
    private String sort;

    private Integer pageNum = 1;
    /*** 原生所有查询属性*/
    private String _queryString;
}
