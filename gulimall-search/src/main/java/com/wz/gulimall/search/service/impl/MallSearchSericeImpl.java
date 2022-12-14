package com.wz.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wz.common.to.es.SkuEsModel;
import com.wz.common.utils.R;
import com.wz.gulimall.config.GuliEsConfig;
import com.wz.gulimall.search.constant.EsConstant;
import com.wz.gulimall.search.feign.ProductFeignService;
import com.wz.gulimall.search.service.MallSearchSerice;
import com.wz.gulimall.search.vo.AttrResponseVo;
import com.wz.gulimall.search.vo.BrandVo;
import com.wz.gulimall.search.vo.SearchParam;
import com.wz.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchSericeImpl implements MallSearchSerice {
    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult searchResult = null;
        SearchRequest searchRequest = this.handleSearchRequest(searchParam);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GuliEsConfig.COMMON_OPTIONS);
            searchResult = handleSearchResponse(searchResponse, searchParam);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return searchResult;
    }

    private SearchRequest handleSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        ????????????
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
//        ??????
        if (searchParam.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        if (searchParam.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
//            100_300
            String[] skuPrices = searchParam.getSkuPrice().split("_");
            if (skuPrices.length == 1) {
                if (searchParam.getSkuPrice().startsWith("_")) {
                    rangeQueryBuilder.gte(skuPrices[0]);
                }
                if (searchParam.getSkuPrice().endsWith("_")) {
                    rangeQueryBuilder.lte(skuPrices[0]);
                }
            } else {
                rangeQueryBuilder.gte(skuPrices[0]).lte(skuPrices[1]);
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

//        ????????????
//        attrs=1_34:sd
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            for (String attr : searchParam.getAttrs()) {
                BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();
                String[] attrStr = attr.split("_");
                String attrId = attrStr[0];
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("attrs.attrId", attrId);
                boolQueryBuilder1.must(termQueryBuilder);
                String[] attrValues = attrStr[1].split(":");
                TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("attrs.attrValue", attrValues);
                boolQueryBuilder1.must(termsQueryBuilder);
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", boolQueryBuilder1, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String[] sortStr = searchParam.getSort().split("_");
            SortOrder sortOrder = sortStr[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(sortStr[0], sortOrder);
        }

//        ??????
        int from = (searchParam.getPageNum() - 1) * EsConstant.ES_PAGE_SIZE;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(EsConstant.ES_PAGE_SIZE);
//        ??????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("skuTitle");
        highlightBuilder.preTags("<b style='color:red'>");
        highlightBuilder.postTags("</b>");
        searchSourceBuilder.highlighter(highlightBuilder);

//        ??????
        TermsAggregationBuilder aggregationBrandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        aggregationBrandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        aggregationBrandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(aggregationBrandAgg);

        TermsAggregationBuilder aggregationCatalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(50);
        aggregationCatalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(aggregationCatalogAgg);

        NestedAggregationBuilder nestedAggregationAttrsAgg = AggregationBuilders.nested("attr_agg", "attrs");
        AggregationBuilder aggregationBuildersAttrId = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        aggregationBuildersAttrId.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        aggregationBuildersAttrId.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(1));
        nestedAggregationAttrsAgg.subAggregation(aggregationBuildersAttrId);
        searchSourceBuilder.aggregation(nestedAggregationAttrsAgg);

        System.out.println(searchSourceBuilder);
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }

    private SearchResult handleSearchResponse(SearchResponse searchResponse, SearchParam searchParam) {
        SearchResult searchResult = new SearchResult();
//        //    ????????????
//        private List<SkuEsModel> products;
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            SkuEsModel skuEsModel = JSON.parseObject(searchHit.getSourceAsString(), SkuEsModel.class);
            if (!StringUtils.isEmpty(searchParam.getKeyword())) {
                skuEsModel.setSkuTitle(searchHit.getHighlightFields().get("skuTitle").getFragments()[0].toString());
            }
            skuEsModelList.add(skuEsModel);
        }
        searchResult.setProducts(skuEsModelList);

//        //    ????????????
//        private Integer pageNum;
//        private Long total;
//        private Integer totalPages;
//        private List<Integer> pageNavs;
        searchResult.setPageNum(searchParam.getPageNum());
        long total = searchResponse.getHits().getTotalHits().value;
        searchResult.setTotal(total);

        int totalPages = (int) (searchResponse.getHits().getTotalHits().value % EsConstant.ES_PAGE_SIZE == 0 ? searchResponse.getHits().getTotalHits().value / EsConstant.ES_PAGE_SIZE : searchResponse.getHits().getTotalHits().value / EsConstant.ES_PAGE_SIZE + 1);
        searchResult.setTotalPages(totalPages);

        List<Integer> pageNavs = getPageNavs(searchParam.getPageNum(), totalPages);
        searchResult.setPageNavs(pageNavs);
        //
//        List<SearchResult.BrandVo> brands;
        List<SearchResult.BrandVo> brands = new ArrayList<>();
        ParsedLongTerms brand_agg = searchResponse.getAggregations().get("brand_agg");
        brands = brand_agg.getBuckets().stream().map((item) -> {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId((Long) item.getKey());
            ParsedStringTerms brand_img_agg = item.getAggregations().get("brand_img_agg");
            brandVo.setBrandImg(brand_img_agg.getBuckets().get(0).getKeyAsString());
            ParsedStringTerms brand_name_agg = item.getAggregations().get("brand_name_agg");
            brandVo.setBrandName(brand_name_agg.getBuckets().get(0).getKeyAsString());
            return brandVo;
        }).collect(Collectors.toList());
        searchResult.setBrands(brands);
//        List<SearchResult.AttrVo> attrs;//????????????
        List<SearchResult.AttrVo> attrs = new ArrayList<>();
        ParsedNested parsedNested = searchResponse.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = parsedNested.getAggregations().get("attr_id_agg");
        attrs = attr_id_agg.getBuckets().stream().map((item) -> {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId(item.getKeyAsNumber().longValue());
            ParsedStringTerms attr_name_agg = item.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attr_name_agg.getBuckets().get(0).getKeyAsString());
            ParsedStringTerms attr_value_agg = item.getAggregations().get("attr_value_agg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            return attrVo;
        }).collect(Collectors.toList());
        searchResult.setAttrs(attrs);
//        List<SearchResult.CatalogVo> catalogs;//????????????
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = searchResponse.getAggregations().get("catalog_agg");
        catalog_agg.getBuckets().forEach(item -> {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId((Long) item.getKey());
            ParsedStringTerms catalog_name_agg = item.getAggregations().get("catalog_name_agg");
            catalogVo.setCatalogName(catalog_name_agg.getBuckets().get(0).getKeyAsString());
            catalogVos.add(catalogVo);
        });
        searchResult.setCatalogs(catalogVos);
//      ??????????????? //???????????????????????????????????????????????????????????????????????????????????????????????????
        List<SearchResult.NavVo> navs = new ArrayList<>();
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            navs = searchParam.getAttrs().stream().map((item) -> {
//            1_???:???
                String[] splits = item.split("_");
                searchResult.getAttrIds().add(Long.parseLong(splits[0]));
                SearchResult.NavVo navVo = new SearchResult.NavVo();
//            ????????????product??????????????????????????????
                navVo.setNavValue(splits[1]);
                R r = productFeignService.attrInfo(Long.parseLong(splits[0]));

                if (r.getCode() == 0) {
                    AttrResponseVo attrRespVo = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(attrRespVo.getAttrName());
                } else {
                    navVo.setNavName(splits[0]);
                }
//                ???????????????
                String replace = replaceQueryString(searchParam, item, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html" + (StringUtils.isEmpty(replace) ? "" : "?" + replace));
                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavs(navs);
        }
//            ????????????
//            ???????????????????????????
//            ?????????????????????????????????
//            ??????brandIdList
        List<Long> brandIds = searchParam.getBrandId();
        if (brandIds != null && brandIds.size() > 0) {
//            ???product????????????BrandVoList???BrandId???Brandname??????????????????
            R brandR = productFeignService.BrandInfos(brandIds);
            if (brandR.getCode() == 0) {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
//            ???????????????ID??????
                navVo.setNavName("??????");
                StringBuffer sb = new StringBuffer();
                List<BrandVo> brandVos = brandR.getData("brands", new TypeReference<List<BrandVo>>() {
                });
//            ?????????????????????
                String replace = "";
                for (BrandVo brandVo : brandVos) {
//            ??????link
                    sb.append(brandVo.getName()).append(";");
                    replace = replaceQueryString(searchParam, String.valueOf(brandVo.getBrandId()), "brandId");
                    searchParam.set_queryString(replace);
                }
                navVo.setNavValue(sb.toString().substring(0, sb.length() - 1));
                navVo.setLink("http://search.gulimall.com/list.html" + (StringUtils.isEmpty(replace) ? "" : "?" + replace));
                searchResult.getNavs().add(navVo);
            }
        }
//
//            ???replace????????????Url?????????brandId = brandId


        return searchResult;
    }

    private String replaceQueryString(SearchParam searchParam, String item, String key) {
        String _queryString = searchParam.get_queryString();
        String encodeAttrValue = null;
        try {
            encodeAttrValue = URLEncoder.encode(item, "UTF-8");
            encodeAttrValue.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String replace = _queryString.replace("&" + key + "=" + encodeAttrValue, "").replace(key + "=" + encodeAttrValue, "");
        return replace;
    }

    private List<Integer> getPageNavs(int pageNum, int totalPages) {
        Integer begin = 0;
        Integer end = 5;
        begin = pageNum - 4;
        end = pageNum + 5;
        if (begin < 1) {
            begin = 1;
        }
        if (end > totalPages) {
            end = totalPages;
        }
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = begin; i <= end; i++) {
            pageNavs.add(i);
        }
        return pageNavs;
    }
}
