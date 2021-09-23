package com.stu.manage.demo.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.stu.manage.demo.Exception.CommonEnum;
import com.stu.manage.demo.Exception.JkyException;
import com.stu.manage.demo.entity.LoginToken;
import com.stu.manage.demo.service.LoginService;
import com.stu.manage.demo.util.DESUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.config
 * @desc
 * @date 2021/8/27 14:27
 * @Copyright © 河南交科院
 */
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse, Object object) {
        String token = httpServletRequest.getHeader ("token");// 从 http 请求头中取出 token
        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod ();
        //检查是否有@passtoken注解，有则跳过认证
        if (method.isAnnotationPresent (PassToken.class)) {
            PassToken passToken = method.getAnnotation (PassToken.class);
            if (passToken.required ()) {
                return true;
            }
        }else {
            // 执行认证
            if (token == null) {
                throw new JkyException(CommonEnum.SIGNATURE_NOT_MATCH);
            }
            // 获取 token 中的 user id
            String adminId;
            try {
                adminId = JWT.decode (token).getAudience ().get (0);
            } catch (JWTDecodeException j) {
                throw new RuntimeException ("401");
            }
            LoginToken admin = loginService.getUser(adminId);
            String decode = DESUtils.decrypt(admin.getPassWord(),admin.getAdminId());
            if (admin == null) {
                throw new JkyException ("用户不存在");
            }
            // 验证 token
            JWTVerifier jwtVerifier = JWT.require (Algorithm.HMAC256 (decode)).build ();
            try {
                jwtVerifier.verify (token);
            } catch (JWTVerificationException e) {
                throw new JkyException ("401","token无效或非法");
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception { }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception { }
}
