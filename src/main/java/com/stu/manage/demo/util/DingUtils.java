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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            logger.debug("获取服务窗数据成功:{}",JSON.toJSONString(response));
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
            logger.debug("发送客户服务窗消息成功:{}",JSON.toJSONString(response));
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
}
