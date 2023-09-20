package com.lims.manage.erp.job;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.lims.manage.erp.util.AccessTokenSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.job
 * @desc
 * @date 2023-09-14 10:30
 * @Copyright © 河南交科院
 */
@Component
public class DingMessageJob {
    @Value("${dingtalk.token_url}")
    private String tokenUrl;
    @Value("${dingtalk.app_key}")
    private String appKey;
    @Value("${dingtalk.app_secret}")
    private String appsecret;

    @Async("syncExecutor1")
    //@Scheduled(cron="0 0 6 1/1 * ?")
    @Scheduled(cron="0 0/1 * * * ?")
    public void OAWorkNotice() throws Exception {
        AccessTokenSingleton instance = AccessTokenSingleton.getInstance();
        String token = instance.getToken(tokenUrl, "dingxg4nevxtzc90q9as", "ncCJeNny61UEH4LoTf-Pk54jDwj34fPWO0ZhmwEILSva0NbGd4WasCcp3aEDw_9Z");

        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setAgentId(2729791632L);
        request.setUseridList("031256115936408398");
        request.setToAllUser(false);

        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        OapiMessageCorpconversationAsyncsendV2Request.OA oa = new OapiMessageCorpconversationAsyncsendV2Request.OA();


        OapiMessageCorpconversationAsyncsendV2Request.Head head = new OapiMessageCorpconversationAsyncsendV2Request.Head();
        head.setBgcolor("FFFF6A00");
        head.setText("lims检测系统任务池有新的任务注入");
        OapiMessageCorpconversationAsyncsendV2Request.Body body = new OapiMessageCorpconversationAsyncsendV2Request.Body();
        List<OapiMessageCorpconversationAsyncsendV2Request.Form> list = new ArrayList<>();
        OapiMessageCorpconversationAsyncsendV2Request.Form form1 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
        form1.setKey("任务发布人:");
        form1.setValue("郭家林");

        list.add(form1);
        body.setForm(list);
        body.setTitle("有新的检测任务待领取");
        body.setImage("@lADOADmaWMzazQKA");

        oa.setPcMessageUrl("http://192.168.2.23:8082/jkyErp/index.html#/");
        oa.setBody(body);
        oa.setHead(head);
        msg.setOa(oa);
        msg.setMsgtype("oa");
        request.setMsg(msg);
        OapiMessageCorpconversationAsyncsendV2Response rsp = client.execute(request, token);
        System.out.println(rsp.getBody());
    }

}
