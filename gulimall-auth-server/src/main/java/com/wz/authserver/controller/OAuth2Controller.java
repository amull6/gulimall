package com.wz.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wz.authserver.feign.MemberFeignService;
import com.wz.common.constant.AuthServerConstant;
import com.wz.common.vo.MemberResVo;
import com.wz.authserver.vo.SocialUser;
import com.wz.common.utils.HttpUtils;
import com.wz.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
@Slf4j
@Controller
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;

    /*
     *根据code登录或者获取tocken注册
     */
    @RequestMapping("oauth2.0/weibo/success")
    public String weiBo(@RequestParam("code") String code, HttpSession httpSession) throws Exception {
//        根据code向微博发起http请求查询token id;
        HashMap<String, String> map = new HashMap<>();
        map.put("client_id", "3826431200");
        map.put("client_secret", "c4c00c58bc879eaabd9470fc3ecc556c");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        HttpResponse httpResponse = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token",  new HashMap<>(), null, map);
//        成功
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            String jsonStr = EntityUtils.toString(httpResponse.getEntity());
            SocialUser socialUser = JSON.parseObject(jsonStr, SocialUser.class);
//          1根据id远程调用member服务查询出用户
            R oauthLogin = memberFeignService.login(socialUser);
            if (oauthLogin.getCode() == 0) {
                MemberResVo memberResVo = oauthLogin.getData(new TypeReference<MemberResVo>() {
                });
                log.info("登录成功：用户：{}", memberResVo.toString());
//             用户放入session
                httpSession.setAttribute(AuthServerConstant.LOGIN_USER, memberResVo);
//        返回到主页
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
//        失败返回到登录页面
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
