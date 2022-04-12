package com.lims.manage.erp.shiro;

import com.lims.manage.erp.Exception.JkyException;
import com.lims.manage.erp.util.RedisUtils;
import com.lims.manage.erp.util.ShiroUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;
import java.util.Collection;

/**
 * @Description 自定义获取Token
 * @Author gjl
 * @CreateTime 2021/11/09 8:34
 */
public class ShiroSessionManager extends DefaultWebSessionManager {
    @Autowired
    private RedisUtils redisUtils;
    //定义常量
    private static final String AUTHORIZATION = "Authorization";
    private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request";
    //重写构造器
    public ShiroSessionManager() {
        super();
        this.setDeleteInvalidSessions(true);
    }
    /**
     * 重写方法实现从请求头获取Token便于接口统一
     * 每次请求进来,Shiro会去从请求头找Authorization这个key对应的Value(Token)
     * @Author gjl
     * @CreateTime 2021/11/09 8:47
     */
    @Override
    public Serializable getSessionId(ServletRequest request, ServletResponse response) {
        String requestURI = WebUtils.toHttp(request).getRequestURI();
        String token = WebUtils.toHttp(request).getHeader(AUTHORIZATION);
        //如果请求头中存在token 则从请求头中获取token
        if (StringUtils.isNotEmpty(token)) {
            //校验token是否存在
            String key = "shiro:session:" + token;
            Object o = redisUtils.get("\""+key+"\"");
            if (o == null){
                throw new JkyException("token不合法或已过期");
            }
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, REFERENCED_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, token);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
            return token;
        } else {
            if (requestURI.contains("/userLogin/") || requestURI.contains("/qiyuesuo/")){
                return null;
            }else {
                throw new JkyException("token信息为空");
            }
        }
    }
}