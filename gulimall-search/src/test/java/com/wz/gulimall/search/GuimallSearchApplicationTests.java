package com.wz.gulimall.search;


import com.alibaba.fastjson.JSON;
import com.wz.gulimall.config.GuliEsConfig;
import lombok.Data;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GuimallSearchApplicationTests {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Test
    public void contextLoads() throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("gulimall_product");
        String json = "{ \"mappings\":{ \"properties\":{ \"skuId\":{ \"type\":\"long\" }, \"spuId\":{ \"type\":\"keyword\" },\"skuTitle\":{ \"type\":\"text\", \"analyzer\":\"ik_smart\"}, \"skuPrice\":{ \"type\":\"keyword\" }, \"skuImg\"  :{ \"type\":\"keyword\" }, \"saleCount\":{ \"type\":\"long\" }, \"hasStock\":{ \"type\":\"boolean\" }, \"hotScore\":{ \"type\":\"long\"  }, \"brandId\": { \"type\":\"long\" }, \"catalogId\":{ \"type\":\"long\"  }, \"brandName\":{\"type\":\"keyword\"},\"brandImg\":{ \"type\":\"keyword\", \"index\":false}, \"catalogName\":{\"type\":\"keyword\" },\"attrs\":{ \"type\":\"nested\", \"properties\":{ \"attrId\":{\"type\":\"long\"  }, \"attrName\":{ \"type\":\"keyword\", \"index\":false}, \"attrValue\":{\"type\":\"keyword\" } } } } } }";
        createIndexRequest.source(json, XContentType.JSON);
        restHighLevelClient.indices().create(createIndexRequest, GuliEsConfig.COMMON_OPTIONS);
    }
    @Test
    public void testDelete() throws IOException {
//       删除索引
        restHighLevelClient.indices().delete(new DeleteIndexRequest("gulimall_product"), GuliEsConfig.COMMON_OPTIONS);
    }

    @Test
    public void testGet() throws IOException {
//       删除索引
        boolean is = restHighLevelClient.indices().exists(new GetIndexRequest("gulimall_product"), GuliEsConfig.COMMON_OPTIONS);
        System.out.println(is);
    }
    @Test
    public void testIndex() throws IOException {
        // 设置索引
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");

        User user = new User();
        user.setUsername("张三");
        user.setAge(20);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);

        //设置要保存的内容，指定数据和类型
        indexRequest.source(jsonString, XContentType.JSON);

        //执行创建索引和保存数据
        IndexResponse index = restHighLevelClient.index(indexRequest, GuliEsConfig.COMMON_OPTIONS);

        System.out.println(index);
    }

    @Test
    public void testIndexSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GuliEsConfig.COMMON_OPTIONS);
        System.out.println(searchResponse.toString());
    }

    @Test
    public void testAggregation() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        TermsAggregationBuilder aggregation1 = AggregationBuilders.terms("aggAge")
                .field("age");
        AvgAggregationBuilder aggregation2 = AggregationBuilders.avg("agg02").field("balance");
        aggregation1.subAggregation(aggregation2);
        AvgAggregationBuilder aggregation3 = AggregationBuilders.avg("agg03").field("balance");
        searchSourceBuilder.aggregation(aggregation1);
        searchSourceBuilder.aggregation(aggregation3);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GuliEsConfig.COMMON_OPTIONS);

        System.out.println(searchResponse.toString());

        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit :searchHits) {
            String source = searchHit.getSourceAsString();
            Account account = JSON.parseObject(source, Account.class);
            System.out.println(account);
        }
        Aggregations aggregations = searchResponse.getAggregations();
        Terms byCompanyAggregation = aggregations.get("aggAge");
        for( Terms.Bucket bucket:byCompanyAggregation.getBuckets()){
            Aggregations aggregations2 = bucket.getAggregations();
            Avg averageAge = aggregations2.get("agg02");
            String key = bucket.getKeyAsString();
            System.out.println(bucket.getDocCount());
            System.out.println(key);
            System.out.println(averageAge.getValue());

        }
        Avg averageAge = aggregations.get("agg03");
        System.out.println(averageAge.getValue());
    }

    @Data
    public static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;

        @Override
        public String toString() {
            return "Account{" +
                    "account_number=" + account_number +
                    ", balance=" + balance +
                    ", firstname='" + firstname + '\'' +
                    ", lastname='" + lastname + '\'' +
                    ", age=" + age +
                    ", gender='" + gender + '\'' +
                    ", address='" + address + '\'' +
                    ", employer='" + employer + '\'' +
                    ", email='" + email + '\'' +
                    ", city='" + city + '\'' +
                    ", state='" + state + '\'' +
                    '}';
        }
    }


    @Data
    class User {
        private String username;
        private String gender;
        private Integer age;
    }

}
