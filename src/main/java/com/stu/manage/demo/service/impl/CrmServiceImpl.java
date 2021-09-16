package com.stu.manage.demo.service.impl;

import com.aliyun.dingtalkcrm_1_0.Client;
import com.aliyun.dingtalkcrm_1_0.models.GetOfficialAccountContactsResponse;
import com.stu.manage.demo.service.CrmService;
import com.stu.manage.demo.util.AccessTokenSingleton;
import com.stu.manage.demo.util.DingUtils;
import com.stu.manage.demo.util.GenID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.service.impl
 * @desc
 * @date 2021/9/16 14:35
 * @Copyright © 河南交科院
 */
@Service
public class CrmServiceImpl implements CrmService {
    Logger logger = LoggerFactory.getLogger(CrmServiceImpl.class);

    @Override
    public void getCustomer(String tokenUrl, String appKey, String appsecret) throws Exception {
        String token = AccessTokenSingleton.getInstance().getToken(tokenUrl, appKey, appsecret);
        GetOfficialAccountContactsResponse response = DingUtils.getServiceWindow(token, "10");
        if (response != null){
            //发送消息
            Boolean aBoolean = DingUtils.sendMessageToServiceWindow(token,"账号/密码：psh/111111", GenID.getUUID(),
                    "sxmwx7mtl2cnzvmxlwmnznpzty");
            if (aBoolean){
                logger.debug("消息发送成功。。。。。。。。。。。。。。");
            }
        }
    }
}
