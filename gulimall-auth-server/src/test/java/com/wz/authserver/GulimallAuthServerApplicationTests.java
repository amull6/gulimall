package com.wz.authserver;

import com.wz.common.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@SpringBootTest
@RunWith(SpringRunner.class)
class GulimallAuthServerApplicationTests {

    @Test
    void contextLoads() throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("client_id", "3826431200");
        map.put("client_secret", "c4c00c58bc879eaabd9470fc3ecc556c");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http%3A%2F%2Fauth.gulimall.com%2Foauth2.0%2Fweibo%2Fsuccess");
        map.put("code", "123334324234");
        HttpResponse httpResponse = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token",  new HashMap<>(), null, map);
        System.out.println(httpResponse.getStatusLine().getStatusCode());
    }

}
