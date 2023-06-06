package com.cpiwx.mqttstarter.interceptors;

import cn.hutool.core.util.IdUtil;
import com.cpiwx.mqttstarter.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        log.debug("preHandle");
        RequestUtil.setUserInfo(IdUtil.simpleUUID());
        return true;
    }

    // @Override
    // public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
    //                        @Nullable ModelAndView modelAndView) throws Exception {
    // }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        RequestUtil.clear();
    }
}
