package com.stu.manage.demo.controller;

import com.stu.manage.demo.service.CrmService;
import com.stu.manage.demo.util.AccessTokenSingleton;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aliyun.tea.*;
import com.aliyun.fnf20190315.*;
import com.aliyun.fnf20190315.models.*;
import com.aliyun.teaopenapi.*;
import com.aliyun.teaopenapi.models.*;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.controller
 * @desc
 * @date 2021/9/15 10:17
 * @Copyright © 河南交科院
 */
@RestController
@Slf4j
@RequestMapping("/crm/")
public class CrmController {
    Logger logger = LoggerFactory.getLogger(CrmController.class);
    @Autowired
    private CrmService crmService;

    @Value("${dingtalk.token_url}")
    private String tokenUrl;
    @Value("${dingtalk.app_key}")
    private String appKey;
    @Value("${dingtalk.app_secret}")
    private String appsecret;

    /**
     * 获取服务窗客户信息
     * @throws Exception
     */
    @GetMapping("getCustomer")
    public  void testCrm() throws Exception {
        crmService.getCustomer(tokenUrl,appKey,appsecret);


    }

}
