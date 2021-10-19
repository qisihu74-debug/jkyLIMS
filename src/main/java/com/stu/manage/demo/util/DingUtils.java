package com.stu.manage.demo.util;

import com.alibaba.fastjson.JSON;
import com.aliyun.dingtalkcrm_1_0.models.GetOfficialAccountContactsHeaders;
import com.aliyun.dingtalkcrm_1_0.models.GetOfficialAccountContactsRequest;
import com.aliyun.dingtalkcrm_1_0.models.GetOfficialAccountContactsResponse;
import com.aliyun.dingtalkcrm_1_0.models.SendOfficialAccountOTOMessageHeaders;
import com.aliyun.dingtalkcrm_1_0.models.SendOfficialAccountOTOMessageRequest;
import com.aliyun.dingtalkcrm_1_0.models.SendOfficialAccountOTOMessageResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.taobao.api.ApiException;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.util
 * @desc
 * @date 2021/9/16 13:51
 * @Copyright © 河南交科院
 */
public class DingUtils {
    static Logger logger = LoggerFactory.getLogger(DingUtils.class);
    /**
     * 使获取请求服务窗 Client
     * @return Client
     * @throws Exception
     */
    public static com.aliyun.dingtalkcrm_1_0.Client createClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkcrm_1_0.Client(config);
    }

    /**
     * 获取服务窗数据
     * @param accesToken
     * @param nextIndex
     * @return
     * @throws Exception
     */
    public static GetOfficialAccountContactsResponse getServiceWindow(String accesToken,String nextIndex) throws Exception {
        com.aliyun.dingtalkcrm_1_0.Client client = DingUtils.createClient();
        GetOfficialAccountContactsHeaders getOfficialAccountContactsHeaders = new GetOfficialAccountContactsHeaders();
        getOfficialAccountContactsHeaders.xAcsDingtalkAccessToken = accesToken;
        GetOfficialAccountContactsRequest getOfficialAccountContactsRequest = new GetOfficialAccountContactsRequest()
                .setNextToken(nextIndex).setMaxResults(10L);
        GetOfficialAccountContactsResponse response = null;
        try {
            response = client.getOfficialAccountContactsWithOptions(getOfficialAccountContactsRequest, getOfficialAccountContactsHeaders, new RuntimeOptions());
            logger.info("获取服务窗数据成功:{}",JSON.toJSONString(response));
        } catch (TeaException err) {
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                logger.error("获取服务窗数据失败:{}",JSON.toJSONString(err));
            }
        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                logger.error("获取服务窗数据失败:{}",JSON.toJSONString(_err));
            }
        }
        return response;
    }

    /**
     * 推送信息到指定客户的服务窗
     * @param accesToken
     * @param content 消息内容
     * @param uuid 唯一标识
     * @param usreId 客户的id
     * @return
     * @throws Exception
     */
    public static Boolean sendMessageToServiceWindow(String accesToken,String content,String uuid, String usreId) throws Exception {
        Boolean flag = true;
        com.aliyun.dingtalkcrm_1_0.Client client = DingUtils.createClient();
        SendOfficialAccountOTOMessageHeaders sendOfficialAccountOTOMessageHeaders = new SendOfficialAccountOTOMessageHeaders();
        sendOfficialAccountOTOMessageHeaders.xAcsDingtalkAccessToken = accesToken;
       /* */
      /*  SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetailMessageBodyActionCard detailMessageBodyActionCard = new SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetailMessageBodyActionCard()
                .setButtonOrientation("1")
                .setSingleUrl("https://open.dingtalk.com")
                .setSingleTitle("查看详情")
                .setMarkdown("支持markdown格式的正文内容")
                .setTitle("透出到会话列表和通知的文案")
                .setButtonList(java.util.Arrays.asList(
                        detailMessageBodyActionCardButtonList0
                ));*/
        /*SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetailMessageBodyLink detailMessageBodyLink = new SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetailMessageBodyLink()
                .setPicUrl("@lADOADmaWMzazQKA")
                .setMessageUrl("https://www.dingtalk.com/")
                .setTitle("link消息标题")
                .setText("link消息内容");*/
/*        SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetailMessageBodyMarkdown detailMessageBodyMarkdown = new SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetailMessageBodyMarkdown()
                .setTitle("欢迎您关注服务窗")
                .setText("# 这是支持markdown的文本 \n## 标题2 \n* 列表1 \n![alt 啊](https://img.alicdn.com/tps/TB1XLjqNVXXXXc4XVXXXXXXXXXX-170-64.png)");*/
        SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetailMessageBodyText detailMessageBodyText = new SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetailMessageBodyText()
                .setContent(content);
        SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetailMessageBody detailMessageBody = new SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetailMessageBody()
                .setText(detailMessageBodyText);
                /*.setMarkdown(detailMessageBodyMarkdown)
                .setLink(detailMessageBodyLink)
                .setActionCard(detailMessageBodyActionCard);*/
        SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetail detail = new SendOfficialAccountOTOMessageRequest.SendOfficialAccountOTOMessageRequestDetail()
                .setMsgType("text")
                .setUuid(uuid)
                .setUserId(usreId)
                .setMessageBody(detailMessageBody);
        SendOfficialAccountOTOMessageRequest sendOfficialAccountOTOMessageRequest = new SendOfficialAccountOTOMessageRequest()
                .setDetail(detail);
        //.setBizId("abc");
        try {
            SendOfficialAccountOTOMessageResponse response = client.sendOfficialAccountOTOMessageWithOptions(sendOfficialAccountOTOMessageRequest, sendOfficialAccountOTOMessageHeaders, new RuntimeOptions());
            logger.info("发送客户服务窗消息成功:{}",JSON.toJSONString(response));
        } catch (TeaException err) {
            flag = false;

            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                logger.error("发送客户服务窗消息失败:{}",JSON.toJSONString(err));
            }
        } catch (Exception _err) {
            flag = false;
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                logger.error("发送客户服务窗消息失败:{}",JSON.toJSONString(_err));
            }
        }
        return flag;
    }

    /**
     * 获取组织内所有部门ID
     * @param accesToken
     * @return
     * @throws Exception
     */
    public static List<Long> getAllDeptId(String accesToken, String deptUrl){
        List<Long> allDeptId = Lists.newArrayList();
        Long deptId = 1l;
        if(allDeptId.isEmpty()){
            List<Long> deptId1 = getDeptId(accesToken, deptUrl, deptId);
            allDeptId.addAll(deptId1);
        }
        if(!allDeptId.isEmpty()){
            for (int i = 0; i < allDeptId.size(); i++) {
                deptId = allDeptId.get(i);
                List<Long> deptId1 = getDeptId(accesToken, deptUrl, deptId);
                allDeptId.addAll(deptId1);
            }
        }
        return allDeptId;
    }

    /**
     * 根据上级部门ID获取所有部门ID
     * @param accesToken
     * @return
     * @throws Exception
     */
    public static List<Long> getDeptId(String accesToken,String deptUrl,Long deptId){
        List<Long> result = Lists.newArrayList();
        try {
            DefaultDingTalkClient client = new DefaultDingTalkClient(deptUrl);
            OapiV2DepartmentListsubidRequest req = new OapiV2DepartmentListsubidRequest();
            req.setDeptId(deptId);
            OapiV2DepartmentListsubidResponse rsp = client.execute(req,accesToken);
            OapiV2DepartmentListsubidResponse.DeptListSubIdResponse result1 = rsp.getResult();
            result= result1.getDeptIdList();
            System.out.println(rsp.getBody());
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据部门ID获取该部门下用户的ID
     * @param accesToken
     * @param userUrl
     * @param deptId
     * @return
     */
    public static List<String> getUserId(String accesToken,String userUrl,Long deptId){
        List<String> result = Lists.newArrayList();
        try {
            DingTalkClient client = new DefaultDingTalkClient(userUrl);
            OapiUserListidRequest req = new OapiUserListidRequest();
            req.setDeptId(deptId);
            OapiUserListidResponse rsp = client.execute(req, accesToken);
            OapiUserListidResponse.ListUserByDeptResponse result1 = rsp.getResult();
            result = result1.getUseridList();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据用户ID获取用户单天的步数--单次调用最多50人
     * @param accesToken
     * @param stepsUrl
     * @param userIdsStr
     * @param date
     * @return
     */
    public static List<OapiHealthStepinfoListbyuseridResponse.BasicStepInfoVo> getUserSteps(String accesToken, String stepsUrl, String userIdsStr, String date){
        List<OapiHealthStepinfoListbyuseridResponse.BasicStepInfoVo> result = Lists.newArrayList();
        try {
            DingTalkClient client = new DefaultDingTalkClient(stepsUrl);
            OapiHealthStepinfoListbyuseridRequest req = new OapiHealthStepinfoListbyuseridRequest();
            req.setUserids(userIdsStr);
            req.setStatDate(date);
            OapiHealthStepinfoListbyuseridResponse rsp = client.execute(req, accesToken);
            result = rsp.getStepinfoList();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据用户ID获取用户信息
     * @param accesToken
     * @param userInfoUrl
     * @param userIdsStr
     * @return
     */
    public static OapiV2UserGetResponse.UserGetResponse getUserInfo(String accesToken, String userInfoUrl, String userIdsStr){
        OapiV2UserGetResponse.UserGetResponse result = new OapiV2UserGetResponse.UserGetResponse();
        try {
            DingTalkClient client = new DefaultDingTalkClient(userInfoUrl);
            OapiV2UserGetRequest req = new OapiV2UserGetRequest();
            req.setUserid(userIdsStr);
            OapiV2UserGetResponse rsp = client.execute(req, accesToken);

            result = rsp.getResult();
            System.out.println(result.getName());
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 查询所有用户的ID和姓名
     * @param accesToken
     * @param userUrl
     * @param deptId
     * @return
     */
    public static List<OapiUserListsimpleResponse.ListUserSimpleResponse> getAllUserNameAndId(String accesToken, String userUrl, Long deptId){
        List<OapiUserListsimpleResponse.ListUserSimpleResponse> result = Lists.newArrayList();
        Long cursor = 0L;
        boolean hasMore = true;
        while (hasMore){
            OapiUserListsimpleResponse rsp = getUserNameAndId(accesToken, userUrl, deptId, cursor);
            if(rsp.getResult() != null){
                result.addAll(rsp.getResult().getList());
                hasMore = rsp.getResult().getHasMore();
                cursor = rsp.getResult().getNextCursor();
            }
        }
        return result;
    }


    /**
     * 获取部门下用户的ID和姓名
     * @param accesToken
     * @param userUrl
     * @param deptId
     * @param cursor
     * @return
     */
    public static OapiUserListsimpleResponse getUserNameAndId(String accesToken, String userUrl, Long deptId,Long cursor){
        OapiUserListsimpleResponse result = new OapiUserListsimpleResponse();
        try {
            DingTalkClient client = new DefaultDingTalkClient(userUrl);
            OapiUserListsimpleRequest req = new OapiUserListsimpleRequest();
            req.setDeptId(deptId);
            req.setCursor(cursor);
            req.setSize(100L);
            req.setContainAccessLimit(true);
            req.setLanguage("zh_CN");
            OapiUserListsimpleResponse rsp = client.execute(req, accesToken);
            result = rsp;
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return result;
    }

}
