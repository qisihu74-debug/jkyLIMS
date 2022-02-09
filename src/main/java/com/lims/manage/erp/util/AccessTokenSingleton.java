package com.lims.manage.erp.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiDepartmentListRequest;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiUserListbypageRequest;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiUserListbypageResponse;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.DingUserEntity;
import com.taobao.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    /**
     * 获取钉钉用户信息
     * @param userUrl
     * @param token
     * @return
     */
    public List<DingUserEntity> getUserList(String userUrl, String token, String deptUrl) throws ApiException{
        List<DingUserEntity> list = new ArrayList<DingUserEntity>();
        List<DingDeptEntity> list_department = this.getDeptList(deptUrl,token);
        try {
            for (int i=0;i<list_department.size();i++){
                DingTalkClient client = new DefaultDingTalkClient(userUrl);
                OapiUserListbypageRequest req = new OapiUserListbypageRequest();
                req.setHttpMethod("GET");
                req.setDepartmentId(list_department.get(i).getId());
                req.setOffset(0L);
                req.setSize(10L);
                OapiUserListbypageResponse rsp = client.execute(req, token);
                if (rsp.getErrcode() == 0){
                    System.out.println(list_department.get(i).getName());
                    for (int j=0;j<rsp.getUserlist().size();j++){
                        DingUserEntity taobaoUser = new DingUserEntity();
                        taobaoUser.setActive((rsp.getUserlist().get(j).getActive())?1:0);
                        taobaoUser.setAvatar(rsp.getUserlist().get(j).getAvatar());
                        taobaoUser.setDepartment(rsp.getUserlist().get(j).getDepartment().toString().replace("[", "").replace("]", ""));
                        taobaoUser.setDingid(rsp.getUserlist().get(j).getDingId());
                        taobaoUser.setEmail(rsp.getUserlist().get(j).getEmail());
                        taobaoUser.setExtattr(rsp.getUserlist().get(j).getExtattr());
                        taobaoUser.setHireddate(rsp.getUserlist().get(j).getHiredDate());
                        taobaoUser.setIsadmin((rsp.getUserlist().get(j).getIsAdmin())?1:0);
                        taobaoUser.setIsboss((rsp.getUserlist().get(j).getIsBoss())?1:0);
                        taobaoUser.setIshide((rsp.getUserlist().get(j).getIsHide())?1:0);
                        taobaoUser.setIsleader((rsp.getUserlist().get(j).getIsLeader())?1:0);
                        taobaoUser.setJobnumber(rsp.getUserlist().get(j).getJobnumber());
                        taobaoUser.setMobile(rsp.getUserlist().get(j).getMobile());
                        taobaoUser.setName(rsp.getUserlist().get(j).getName());
                        taobaoUser.setOrders(rsp.getUserlist().get(j).getOrder());
                        taobaoUser.setOrgemail(rsp.getUserlist().get(j).getOrgEmail());
                        taobaoUser.setPosition(rsp.getUserlist().get(j).getPosition());
                        taobaoUser.setRemark(rsp.getUserlist().get(j).getRemark());
                        taobaoUser.setTel(rsp.getUserlist().get(j).getTel());
                        taobaoUser.setUnionid(rsp.getUserlist().get(j).getUnionid());
                        taobaoUser.setUserid(rsp.getUserlist().get(j).getUserid());
                        taobaoUser.setWorkplace(rsp.getUserlist().get(j).getWorkPlace());
                        list.add(taobaoUser);
                    }
                }
            }
        } catch (ApiException e) {
            logger.error("获取人员数据异常:{}",e);
        }
        //处理多个部门
        /*List<DingUserEntity> newList = Lists.newArrayList();
        for (DingUserEntity entity:list) {
            String department = entity.getDepartment();
            if (department.contains(",")){
                String[] split = department.replace("[", "").replace("]", "").split(",");
                for (String deptId:split) {
                    entity.setDepartment(deptId);
                    newList.add(entity);
                }
            }else {
                entity.setDepartment(department.replace("[","").replace("]",""));
                newList.add(entity);
            }
        }*/
        return list;
    }

}
