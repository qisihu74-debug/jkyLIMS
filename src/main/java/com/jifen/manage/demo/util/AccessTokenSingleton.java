package com.jifen.manage.demo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiDepartmentGetRequest;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiUserGetuserinfoRequest;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.response.OapiDepartmentGetResponse;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiUserGetuserinfoResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.jifen.manage.demo.result.GatewayResult;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.print.Book;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.jifen.manage.demo.util
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
     * 获取钉钉用户id
     *
     * @param user
     * @param code
     * @param token
     * @return
     */
    public OapiUserGetuserinfoResponse getDingUser(String user, String code, String token) {
        OapiUserGetuserinfoResponse rsp1 = null;
        try {
            DingTalkClient client = new DefaultDingTalkClient(user);
            OapiUserGetuserinfoRequest req = new OapiUserGetuserinfoRequest();
            req.setCode(code);
            req.setHttpMethod("GET");
            rsp1 = client.execute(req, token);
            logger.info("获取用户成功:{}", JSON.toJSONString(rsp1));
        } catch (Exception e) {
            logger.error("调用钉钉接口获取用户信息失败:{}", e);
        }
        return rsp1;
    }

    /**
     * 获取钉钉企业内部应用用户信息
     *
     * @param userInfo
     * @param userId
     * @param token
     * @return
     */
    public OapiV2UserGetResponse getDingInfo(String userInfo, String userId, String token) {
        OapiV2UserGetResponse rsp2 = null;
        try {
            DingTalkClient client = new DefaultDingTalkClient(userInfo);
            OapiV2UserGetRequest req = new OapiV2UserGetRequest();
            req.setHttpMethod("GET");
            req.setUserid(userId);
            rsp2 = client.execute(req, token);
            logger.info("获取用户详情成功:{}", JSON.toJSONString(rsp2));
        } catch (Exception e) {
            logger.error("调用钉钉接口获取用户详情信息失败:{}", e);
        }
        return rsp2;
    }

    /**
     * 获取钉钉企业用户部门信息
     *
     * @param deptIdList
     * @param dept
     * @param token
     * @return
     */
    public List<OapiDepartmentGetResponse> getDingDept(String dept, List<Long> deptIdList, String token) {
        OapiDepartmentGetResponse rsp3 = null;
        List<OapiDepartmentGetResponse> list = new ArrayList();
        for (Long deptId : deptIdList) {
            try {
                DingTalkClient client = new DefaultDingTalkClient(dept);
                OapiDepartmentGetRequest req = new OapiDepartmentGetRequest();
                req.setHttpMethod("GET");
                req.setId(deptId + "");
                rsp3 = client.execute(req, token);
                list.add(rsp3);
                logger.info("获取部门成功:{}", JSON.toJSONString(rsp3));
            } catch (Exception e) {
                logger.error("调用钉钉接口获取部门信息失败:{}", e);
            }
        }
        return list;
    }

    /**
     * 获取图书详情
     *
     * @param isbn
     * @param bookUrl
     * @param showAppId
     * @param showAppSign
     * @return
     */
    public Book getDetail(String isbn, String bookUrl, String showAppId, String showAppSign) {
        Book book = null;
        try {
            CloseableHttpClient client = null;
            CloseableHttpResponse response = null;
            try {
                HttpGet httpGet = new HttpGet(bookUrl + "?isbn="+isbn+"&showapi_appid="+showAppId+"&showapi_sign="+showAppSign);

                client = HttpClients.createDefault();
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                JSONObject datas = JSONObject.parseObject(result);//转换成JSON格式
                Integer status = (Integer) datas.get("showapi_res_code");//获取返回数据状态，get获取的字段需要根据提供的返回值去获取
                if (status == 0) {//返回的状态
                    JSONObject data = JSONObject.parseObject(datas.get("showapi_res_body").toString());//"data"是根据返回值设定
                    JSONObject jsonObject = JSON.parseObject(data.get("data").toString());
                    book = JSON.parseObject(jsonObject.toJSONString(),Book.class);
                }
            } finally {
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return book;
    }

    /**
     * 更新表单数据
     * @param appType
     * @param systemToken
     * @param userId
     * @param language
     * @param formInstId
     * @param useLatestVersion
     * @param updateFormDataJson
     */
    public void update(String appType
            ,String systemToken
            ,String userId
            ,String language
            ,String formInstId
            ,Boolean useLatestVersion
            ,String updateFormDataJson ) {

        String api = "/yida_vpc/form/updateFormData.json";
        GatewayResult result = null;
        try {
            Map<String, String> param = new HashMap<String, String>();
            param.put("appType", appType);
            param.put("systemToken", systemToken);
            param.put("userId", userId);
            param.put("language", language);
            param.put("formInstId", formInstId);
            //param.put("useLatestVersion", useLatestVersion);
            param.put("updateFormDataJson", updateFormDataJson);
            logger.info("开始更新书籍表单数据:{}",JSON.toJSONString(map));
            result = GatewayRequestUtil.baseRequest(param, api);
            logger.info("更新完成表单数据");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
