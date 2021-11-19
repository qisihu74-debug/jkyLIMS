package com.lims.manage.erp.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiDepartmentListRequest;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.DingUserEntity;
import com.taobao.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.util
 * @desc
 * @date 2021/8/25 15:39
 * @Copyright © 河南交科院
 */
public class AccessTokenSingleton {
    Logger logger = LoggerFactory.getLogger(AccessTokenSingleton.class);

    // 缓存accessToken 和 过期时间的 map
    private Map<String, String> map = new HashMap<String, String>();

    private AccessTokenSingleton() {
    }

    private static AccessTokenSingleton single = null;

    public static AccessTokenSingleton getInstance() {
        if (null == single) {
            single = new AccessTokenSingleton();
        }
        return single;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    /**
     * 获取token
     *
     * @return
     */
    public String getToken(String token, String appkey, String appsecret) {
        AccessTokenSingleton atsl = AccessTokenSingleton.getInstance();
        Map<String, String> map = atsl.getMap();
        String accessToken = map.get("access_token");
        String time = map.get("time");
        Long nowDate = System.currentTimeMillis();
        if (null != accessToken && null != time && nowDate - Long.parseLong(time) < 7200000) {
            // 从缓存中读取accessToken数据
            return accessToken;
        } else {
            try {
                //获取token
                OapiGettokenResponse rsp = null;
                try {
                    DingTalkClient client = new DefaultDingTalkClient(token);
                    OapiGettokenRequest request = new OapiGettokenRequest();
                    request.setAppkey(appkey);
                    request.setAppsecret(appsecret);
                    request.setHttpMethod("GET");
                    rsp = client.execute(request);
                    logger.info("获取token成功:{}", JSON.toJSONString(rsp));
                } catch (Exception e) {
                    logger.error("通过钉钉接口获取企业内部token失败:{}", e);
                }
                accessToken = rsp.getAccessToken();
                // 将信息保存进缓存
                map.put("time", nowDate + "");
                map.put("access_token", accessToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return accessToken;
        }
    }

    /**
     * 获取钉钉组织部门信息
     * @param deptUrl
     * @param token
     * @return
     */
    public List<DingDeptEntity> getDeptList(String deptUrl, String token) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(deptUrl);
        OapiDepartmentListRequest req = new OapiDepartmentListRequest();
        req.setHttpMethod("GET");
        req.setFetchChild(true);
        OapiDepartmentListResponse rsp = client.execute(req, token);
        String jsonString = JSON.toJSONString(rsp.getDepartment());
        List<DingDeptEntity> list = JSONArray.parseArray(jsonString, DingDeptEntity.class);
        return list;
    }

    public List<DingUserEntity> getUserList(){
        return  null;
    }

}
