package com.lims.manage.erp.shiro;

import com.lims.manage.erp.constant.RedisConstant;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;

import java.io.Serializable;

/**
 * @Description 自定义SessionId生成器
 * @Author gjl
 * @CreateTime 2021/11/09 11:48
 */
public class ShiroSessionIdGenerator implements SessionIdGenerator {
    /**
     * 实现SessionId生成
     * @Author gjl
     * @CreateTime 2021/11/09 11:54
     */
    @Override
    public Serializable generateId(Session session) {
        Serializable sessionId = new JavaUuidSessionIdGenerator().generateId(session);
        return String.format(RedisConstant.REDIS_PREFIX_LOGIN, sessionId);
    }
}
