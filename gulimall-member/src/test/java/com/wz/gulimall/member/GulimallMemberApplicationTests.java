package com.wz.gulimall.member;

import com.wz.common.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
public class GulimallMemberApplicationTests {

    @Test
    public void contextLoads() throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("client_id", "3826431200");
        map.put("client_secret", "c4c00c58bc879eaabd9470fc3ecc556c");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri","http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", "123334324234");
        HttpResponse httpResponse = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token",  new HashMap<>(), map,"");
        System.out.println(httpResponse.getStatusLine().getStatusCode());
        System.out.println(EntityUtils.toString(httpResponse.getEntity()));
    }

    @Test
    public void contextLoads2() throws Exception {
        HttpResponse httpResponse = HttpUtils.doGet("http://47.100.105.74:9200", "/customer/external/1",  new HashMap<>(), null);
        System.out.println(httpResponse.getStatusLine().getStatusCode());
    }

}
