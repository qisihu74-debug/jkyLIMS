package com.lims.manage.erp.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: DLC
 * @Date: 2023/4/20 17:21
 */
@Component
public class RedisUtil {


    @Autowired
    RedisUtils redisUtils;

    public  String getRedisToken(String token){
        //如果请求头中存在token 则从请求头中获取token
        if (!StringUtils.isEmpty(token)) {
            if (token.equals("null")){
                return null;
            }
            Object o = redisUtils.get("shiro:session:" + token);
            if (o == null){
                return null;
            }else {
                return token;
            }
        } else {
            return null;
        }
    }
}
