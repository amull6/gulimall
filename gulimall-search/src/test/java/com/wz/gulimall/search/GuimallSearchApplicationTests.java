package com.wz.gulimall.search;


import com.alibaba.fastjson.JSON;
import com.wz.gulimall.config.GuliEsConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
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
    public void contextLoads() {
    }

    @Test
    public void testIndex() throws IOException {
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.id("1");
        User user = new User();
        user.setAge(18);
        user.setUsername("qinjie");
        user.setGender("男");
        String js = JSON.toJSONString(user);
        indexRequest.source(js, XContentType.JSON);
        IndexResponse index  = restHighLevelClient.index(indexRequest, GuliEsConfig.COMMON_OPTIONS);
        System.out.println(index);

    }

    @Data
    class User{
        private String username;
        private String gender;
        private Integer age;
    }

}
