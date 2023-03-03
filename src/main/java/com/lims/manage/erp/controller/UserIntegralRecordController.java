package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.extension.api.ApiController;
import com.lims.manage.erp.constant.CommonConstant;
import com.lims.manage.erp.entity.IntegralRule;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.service.IntegralRuleService;
import com.lims.manage.erp.service.UserIntegralRecordService;
import com.lims.manage.erp.util.RedisUtils;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户获取积分记录控制类
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping("/userIntegralRecord")
public class UserIntegralRecordController extends ApiController {


    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    /**
     * 获取用户签到状态
     * @return Result
     */
    @GetMapping(value = "/getSignInStatus")
    public Result<?> getSignInStatus(){
        return userIntegralRecordService.getSignInStatus();
    }

    /**
     * 用户进行签到
     * @return Result
     */
    @PostMapping(value = "/signIn")
    public Result<?> signIn() {
        return userIntegralRecordService.signIn();
    }
}
