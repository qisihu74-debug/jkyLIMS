package com.lims.manage.erp.util;

import com.alibaba.fastjson.JSON;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.request.OapiMessageCorpconversationGetsendprogressRequest;
import com.dingtalk.api.request.OapiMessageCorpconversationGetsendresultRequest;
import com.dingtalk.api.request.OapiMessageCorpconversationRecallRequest;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendprogressResponse;
import com.dingtalk.api.response.OapiMessageCorpconversationGetsendresultResponse;
import com.dingtalk.api.response.OapiMessageCorpconversationRecallResponse;
import com.taobao.api.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2023-10-17 11:07
 * @Copyright © 河南交科院
 */
@Component
public class DingNotifyUtils {
    @Value("${dingtalk.token_url}")
    private String tokenUrl = "https://oapi.dingtalk.com/gettoken";
    private static final String appKey="dingxg4nevxtzc90q9as";
    private static final String appsecret="ncCJeNny61UEH4LoTf-Pk54jDwj34fPWO0ZhmwEILSva0NbGd4WasCcp3aEDw_9Z";
    private static final String url = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2";
    private static final Long agentId= 2729791632L;

    /**
     * 给指定钉钉用户发送消息
     * @param dingId 用户id
     * @param title 标题
     * @param publisher 消息发布人
     * @throws Exception
     */
    public void OAWorkNotice(String dingId,String title,String publisher,String content) throws Exception {
        AccessTokenSingleton instance = AccessTokenSingleton.getInstance();
        String token = instance.getToken(tokenUrl, appKey, appsecret);

        if (token == null || token.length() == 0) { return; }
        com.alibaba.fastjson.JSONObject headJson = new com.alibaba.fastjson.JSONObject();
        headJson.put("bgcolor", "FFFF6A00");
        headJson.put("text", "lims消息通知");
        com.alibaba.fastjson.JSONObject bodyJson = new com.alibaba.fastjson.JSONObject();
        bodyJson.put("title", title);
        bodyJson.put("content", content);
        if (StringUtils.isNotEmpty(publisher)) {
            com.alibaba.fastjson.JSONArray formArr = new com.alibaba.fastjson.JSONArray();
            com.alibaba.fastjson.JSONObject f1 = new com.alibaba.fastjson.JSONObject();
            f1.put("key", "任务发布人:");
            f1.put("value", publisher);
            formArr.add(f1);
            bodyJson.put("form", formArr);
        }
        com.alibaba.fastjson.JSONObject oaJson = new com.alibaba.fastjson.JSONObject();
        oaJson.put("head", headJson);
        oaJson.put("body", bodyJson);
        com.alibaba.fastjson.JSONObject msgJson = new com.alibaba.fastjson.JSONObject();
        msgJson.put("msgtype", "oa");
        msgJson.put("oa", oaJson);
        com.alibaba.fastjson.JSONObject reqJson = new com.alibaba.fastjson.JSONObject();
        reqJson.put("agent_id", agentId);
        reqJson.put("userid_list", dingId);
        reqJson.put("to_all_user", false);
        reqJson.put("msg", msgJson);
        // 钉钉 SDK client.execute 在本 JVM 返回 null，改直连 HTTP 发送
        String sendResp = cn.hutool.http.HttpRequest.post(url + "?access_token=" + token)
                .body(reqJson.toJSONString())
                .timeout(10000)
                .execute().body();
        System.out.println("消息发送(HTTP):" + sendResp);
    }

    /**
     * 查询消息发送的进度
     * @param agentId
     * @param taskId
     * @throws ApiException
     */
    public void getsendprogress(long agentId,Long taskId) throws ApiException {
        AccessTokenSingleton instance = AccessTokenSingleton.getInstance();
        String token = instance.getToken(tokenUrl, appKey, appsecret);
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/getsendprogress");
        OapiMessageCorpconversationGetsendprogressRequest request  = new OapiMessageCorpconversationGetsendprogressRequest();
        request.setAgentId(agentId);
        request.setTaskId(taskId);
        OapiMessageCorpconversationGetsendprogressResponse response = client.execute(request, token);
        System.out.println("查询消息进度:"+ JSON.toJSONString(response));
    }

    /**
     * 查询消息的发送结果
     * @param agentId
     * @param taskId
     * @throws ApiException
     */
    public void getsendresult(long agentId,Long taskId) throws ApiException {
        AccessTokenSingleton instance = AccessTokenSingleton.getInstance();
        String token = instance.getToken(tokenUrl, appKey, appsecret);
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/getsendresult");
        OapiMessageCorpconversationGetsendresultRequest request  = new OapiMessageCorpconversationGetsendresultRequest();
        request.setAgentId(agentId);
        request.setTaskId(taskId);
        OapiMessageCorpconversationGetsendresultResponse response = client.execute(request, token);
        System.out.println("消息的发送结果:"+ JSON.toJSONString(response));
    }

    /**
     * 消息撤回
     * @param agentId
     * @param taskId
     * @throws ApiException
     */
    public void recall(Long agentId,Long taskId) throws ApiException {
        AccessTokenSingleton instance = AccessTokenSingleton.getInstance();
        String token = instance.getToken(tokenUrl, appKey, appsecret);
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/recall");
        OapiMessageCorpconversationRecallRequest request = new OapiMessageCorpconversationRecallRequest();
        request.setAgentId(2729791632L);
        request.setMsgTaskId(2979769136314L);
        OapiMessageCorpconversationRecallResponse response = client.execute(request, token);
    }
}
