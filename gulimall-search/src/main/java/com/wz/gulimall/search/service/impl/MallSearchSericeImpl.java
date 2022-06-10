package com.wz.gulimall.search.service.impl;

import com.wz.gulimall.config.GuliEsConfig;
import com.wz.gulimall.search.constant.EsConstant;
import com.wz.gulimall.search.service.MallSearchSerice;
import com.wz.gulimall.search.vo.SearchParam;
import com.wz.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Service
public class MallSearchSericeImpl implements MallSearchSerice {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult searchResult = null;
        SearchRequest searchRequest = this.handleSearchRequest(searchParam);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GuliEsConfig.COMMON_OPTIONS);
            searchResult = handleSearchResponse(searchResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return searchResult;
    }

    private SearchRequest handleSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        模糊查询
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
//        过滤
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

//        聚合检索
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

//        分页
        int from = (searchParam.getPageNum() - 1) * EsConstant.ES_PAGE_SIZE;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(EsConstant.ES_PAGE_SIZE);
//        高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("skuTitle");
        highlightBuilder.preTags("<b style='color:red'>");
        highlightBuilder.postTags("</b>");
        searchSourceBuilder.highlighter(highlightBuilder);

//        聚合
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

    private SearchResult handleSearchResponse(SearchResponse searchResponse) {
        return null;
    }
}
