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
    private String tokenUrl;
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
    public void OAWorkNotice(String dingId,String title,String publisher) throws Exception {
        AccessTokenSingleton instance = AccessTokenSingleton.getInstance();
        String token = instance.getToken(tokenUrl, appKey, appsecret);

        DingTalkClient client = new DefaultDingTalkClient(url);
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setAgentId(agentId);
        request.setUseridList(dingId);
        request.setToAllUser(false);

        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        OapiMessageCorpconversationAsyncsendV2Request.OA oa = new OapiMessageCorpconversationAsyncsendV2Request.OA();


        OapiMessageCorpconversationAsyncsendV2Request.Head head = new OapiMessageCorpconversationAsyncsendV2Request.Head();
        head.setBgcolor("FFFF6A00");
        head.setText("lims检测系统任务池有新的任务注入");
        OapiMessageCorpconversationAsyncsendV2Request.Body body = new OapiMessageCorpconversationAsyncsendV2Request.Body();
        if (StringUtils.isNotEmpty(publisher)){
            List<OapiMessageCorpconversationAsyncsendV2Request.Form> list = new ArrayList<>();
            OapiMessageCorpconversationAsyncsendV2Request.Form form1 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
            form1.setKey("任务发布人:");
            form1.setValue(publisher);

            list.add(form1);
            body.setForm(list);
        }
        body.setTitle(title+System.currentTimeMillis());
        body.setImage("@resource/dingtalk/message_image");

        //oa.setPcMessageUrl("http://www.baidu.com");
        oa.setBody(body);
        oa.setHead(head);
        msg.setOa(oa);
        msg.setMsgtype("oa");
        request.setMsg(msg);
        OapiMessageCorpconversationAsyncsendV2Response rsp = client.execute(request, token);
        System.out.println("消息发送成功:"+ JSON.toJSONString(rsp));
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
