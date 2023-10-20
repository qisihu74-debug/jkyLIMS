package com.lims.manage.erp.job;

import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.service.DingUserService;
import com.lims.manage.erp.util.AccessTokenSingleton;
import com.taobao.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;


/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.job
 * @desc
 * @date 2021/11/22 10:48
 * @Copyright © 河南交科院
 */
@Component
public class DingUserJob {
    Logger logger = LoggerFactory.getLogger(DingUserJob.class);
    @Autowired
    private DingUserService service;

    @Value("${dingtalk.token_url}")
    private String tokenUrl;
    @Value("${dingtalk.app_key}")
    private String appKey;
    @Value("${dingtalk.app_secret}")
    private String appsecret;
    @Value("${dingtalk.dept_url}")
    private String deptUrl;
    @Value("${dingtalk.user_url}")
    private String userUrl;

    /**
     * 定时拉取钉钉用户信息0 35 2 ?
     */
    @Async("syncExecutor")
    @Scheduled(cron="30 20 1 * * ?")
    public void sync(){
        AccessTokenSingleton instance = AccessTokenSingleton.getInstance();
        String token = instance.getToken(tokenUrl, appKey, appsecret);
        List<DingUserEntity> userList = null;
        try {
            userList = instance.getUserList(userUrl, token, deptUrl);
        }catch (ApiException e){
            logger.error("获取钉钉用户异常:{}",e);
        }
        //保存数据
        //根据userid去重
        userList = userList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DingUserEntity :: getUserid))), ArrayList::new));
        service.saveOrUpdateBatch(userList);
    }
}
