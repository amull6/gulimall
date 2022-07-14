package com.wz.gulimall.cart.interceptor;

import com.wz.common.constant.AuthServerConstant;
import com.wz.common.constant.CartConstant;
import com.wz.common.vo.MemberResVo;
import com.wz.gulimall.cart.vo.UserInfoTo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        新建threadLocal
        UserInfoTo userInfoTo = new UserInfoTo();
//        判断是否登录
        MemberResVo memberResVo = (MemberResVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberResVo != null) {
            userInfoTo.setUserId(memberResVo.getId());
        }
//        判断cookie是否有user-key
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                userInfoTo.setUserKey(cookie.getValue());
                userInfoTo.setTempUser(true);
            }
        }
//        如果没有userKey，重新生成userKey
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            userInfoTo.setUserKey(UUID.randomUUID().toString().replace("-", ""));
        }
//        新建并保存登录用户
        threadLocal.set(userInfoTo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.isTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
