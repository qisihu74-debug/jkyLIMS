package com.lims.manage.erp.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lims.manage.erp.entity.SysUserEntity;
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
            if (o == null) {
                return null;
            } else {
                return token;
            }
        } else {
            return null;
        }
    }

    /**
     * 根据 token 存储 用户信息
     *
     * @param token
     * @param userData
     * @return
     */
    public Boolean setRedisTokenUser(String token, SysUserEntity userData) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", userData);
        // 过期时间 2个小时
        redisUtils.set(token, jsonObject.toJSONString(), 7200);
        return true;
    }

    /**
     * 根据 token 返回 用户信息
     * @param token
     * @return
     */
    public SysUserEntity getRedisTokenUser(String token) {
        // 过期时间 2个小时
        String jsonObject = (String) redisUtils.get(token);
        if (jsonObject == null) {
            return null;
        }
        JSONObject object = (JSONObject) JSONObject.parse(jsonObject);
        return JSONObject.toJavaObject((JSON) object.get("user"), SysUserEntity.class);
    }


}
