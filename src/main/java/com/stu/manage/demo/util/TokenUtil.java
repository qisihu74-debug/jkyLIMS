package com.stu.manage.demo.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.stu.manage.demo.entity.LoginToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.util
 * @desc
 * @date 2021/8/27 11:45
 * @Copyright © 河南交科院
 */
@Component
public class TokenUtil {

    /**
     * 给用户生成token
     * @param admin
     * @return
     */
    public static String getToken(LoginToken admin) {
        Date start = new Date ();
        long currentTime = System.currentTimeMillis () + 60 * 60 * 1000;//一小时有效时间
        Date end = new Date (currentTime);
        return JWT.create ().withAudience (admin.getAdminId()).withIssuedAt (start)
                .withExpiresAt (end)
                .sign (Algorithm.HMAC256 (admin.getPassWord()));
    }

    public static String getTokenUserId() {
        String token = getRequest().getHeader("token");// 从 http 请求头中取出 token
        String userId = JWT.decode(token).getAudience().get(0);
        return userId;
    }

    /**
     * 获取request
     *
     * @return
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        return requestAttributes == null ? null : requestAttributes.getRequest();
    }
}
