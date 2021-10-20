package com.jifen.manage.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jifen.manage.demo.service.DingService;
import com.jifen.manage.demo.filter.PassToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dingservice/")
public class DingServiceController {
    Logger logger = LoggerFactory.getLogger(DingServiceController.class);
    @Value("${dingtalk.token_url}")
    private String tokenUrl;
    @Value("${dingtalk.dept_url2}")
    private String dept_url;
    @Value("${dingtalk.duserinfo_url}")
    private String userid_url;
    @Value("${dingtalk.steps_url1}")
    private String steps_url1;
    @Value("${dingtalk.userinfo_url}")
    private String userinfo_url;
    @Value("${dingtalk.app_key2}")
    private String appKey;
    @Value("${dingtalk.app_secret2}")
    private String appsecret;
    @Autowired
    private DingService dingService;

    @PassToken
    @RequestMapping("get_all_user_steps_test")
    public String getAllUserStepsTest(String date) throws Exception {
        return JSON.toJSONString(dingService.getAllUserSteps(date, tokenUrl, dept_url, userid_url, steps_url1, userinfo_url, appKey, appsecret));
    }
}
