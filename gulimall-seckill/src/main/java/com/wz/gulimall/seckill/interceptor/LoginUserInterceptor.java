package com.wz.gulimall.seckill.interceptor;

import com.wz.common.constant.AuthServerConstant;
import com.wz.common.vo.MemberResVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        Feign调用放行
        boolean match = new AntPathMatcher().match("/seckill/order", request.getRequestURI());

        if (!match) {
            return true;
        }
//        判断session中有无用户信息
        HttpSession session = request.getSession();
        MemberResVo memberResVo = (MemberResVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberResVo != null) {
            loginUser.set(memberResVo);
//            登录成功
            return true;
        } else {
//            未登录
            session.setAttribute("msg", "请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }

    }
}
