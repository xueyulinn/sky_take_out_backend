package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
//拦截器是组件
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     *///处理器（Handler）是用于处理请求的组件或对象。它可以是一个控制器（Controller）类中的方法、一个Servlet等。处理器负责接收请求并执行相应的业务逻辑，然后生成响应返回给客户端。
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*
        springMVC扫描所有controller类,解析所有映射方法,将每个映射方法封装一个对象HandlerMethod,该类包含所有请求映射方法信息(映射路径/方法名/参数/注解/返回值)
        当请求的资源是静态资源，例如图片、样式表或脚本文件时，不会被映射到 Controller 的方法上，而是由服务器直接返回静态资源文件。
        在这种情况下，handler 可能是一个 ResourceHttpRequestHandler 或类似的处理器，而不是 HandlerMethod。
        */
                    //实例
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            /*
            当请求的资源是静态资源时，通常不需要进行身份验证或其他拦截操作。这是因为静态资源（如图片、样式表或脚本文件）是无状态的，不涉及与用户身份相关的操作。这些资源通常被认为是公开可访问的，不需要进行额外的拦截或身份验证。
            拦截器的主要目的是对请求进行预处理、身份验证、权限检查等操作，并干预请求的执行流程。但对于静态资源请求，这些操作是不必要的，并且可能带来不必要的性能开销。
             */
            return true;//true就是放行
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            BaseContext.setCurrentId(empId);
            log.info("当前员工id：", empId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
