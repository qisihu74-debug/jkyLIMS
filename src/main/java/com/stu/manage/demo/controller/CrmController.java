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

    /**
     * 授权crm数据，授权后加入用户表和分配
     * 账号密码推送到客户 服务窗信息
     * @throws Exception TODO
     */
    @GetMapping("authorize")
    public  void testCrm(String userId) throws Exception {
        crmService.authorize(userId);

    }

}
