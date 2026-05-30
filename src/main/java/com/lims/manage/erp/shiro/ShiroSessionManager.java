package com.lims.manage.erp.shiro;

import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.Exception.CommonEnum;
import com.lims.manage.erp.Exception.JkyException;
import com.lims.manage.erp.config.ShiroConfig;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.util.RedisUtils;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

/**
 * @Description 自定义获取Token
 * @Author gjl
 * @CreateTime 2021/11/09 8:34
 */
@Slf4j
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
    @SneakyThrows
    @Override
    public Serializable getSessionId(ServletRequest request, ServletResponse response) {
        String token = WebUtils.toHttp(request).getHeader(AUTHORIZATION);
        //如果请求头中存在token 则从请求头中获取token
        log.info("token信息:{}",token);
        if (!StringUtils.isEmpty(token)) {
            if (token.equals("null")){
                return null;
            }
            Object o = redisUtils.get("shiro:session:" + token);
            if (o == null){
                Result result = new Result();
                result.setCode(-1);
                result.setMsg("token过期请重新登录！");
                responseJsonString((HttpServletResponse) response,JSON.toJSONString(result));
            }else {
                request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, REFERENCED_SESSION_ID_SOURCE);
                request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, token);
                request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
                return token;
            }
        } else {
            // 这里禁用掉Cookie获取方式
            // 按默认规则从Cookie取Token
            // return super.getSessionId(request, response);
            return null;
        }
        return null;
    }

    /**
     * response相应json数据
     * @param response
     * @param jsonString
     */
    public void responseJsonString(HttpServletResponse response, String jsonString) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        OutputStream os = null;
        byte[] bytes;
        try {
            bytes = jsonString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.setContentLength(bytes.length);
        try {
            os = response.getOutputStream();
            os.write(bytes);
            os.flush();
            if (log.isDebugEnabled()) {
                log.debug("Send the client " + jsonString);
            }
        } catch (IOException e) {
            log.error("Exception happens.", e);
            throw new RuntimeException(e);
        }finally {
            if (os!= null) {
                os.close();
            }
        }
    }
}