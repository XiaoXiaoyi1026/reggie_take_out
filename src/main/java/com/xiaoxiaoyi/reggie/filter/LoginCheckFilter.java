package com.xiaoxiaoyi.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.xiaoxiaoyi.reggie.common.BaseContext;
import com.xiaoxiaoyi.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    // 定义路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        servletRequest.setCharacterEncoding("UTF-8");

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1. 获取请求URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}", requestURI);

        // 定义可放行路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };

        // 2. 判断是否可以直接放行
        boolean check = check(requestURI, urls);
        if (check) {
            log.info("本次请求URI：{}，不需要处理", requestURI);
            // 放行
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 从session中获取用户登录信息，如果有信息则直接放行
        if (request.getSession().getAttribute("employee") != null) {

            Long currentId = (Long) request.getSession().getAttribute("employee");
            log.info("用户已登录，id：{}", currentId);
            // 将当前登录用户的id设置到线程的ThreadLocal中
            BaseContext.setCurrentId(currentId);

            long id = Thread.currentThread().getId();
            log.info("thread id:{}", id);

            // 放行
            filterChain.doFilter(request, response);
            return;
        }

        // 前端登录检查
        if (request.getSession().getAttribute("user") != null) {

            Long userId = (Long) request.getSession().getAttribute("user");
            log.info("前端用户已登录，id：{}", userId);
            // 将当前登录用户的id设置到线程的ThreadLocal中
            BaseContext.setCurrentId(userId);

            long id = Thread.currentThread().getId();
            log.info("thread id:{}", id);

            // 放行
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 返回登录失败的信息
        log.info("用户未登录.");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    public boolean check(String requestURI, String[] urls) {
        for (String url : urls) {
            // 使用路径匹配器进行匹配，如果匹配成功则直接返回true
            if (PATH_MATCHER.match(url, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
