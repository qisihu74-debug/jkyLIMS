package com.lims.manage.erp.job;

import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.service.DeptService;
import com.lims.manage.erp.util.AccessTokenSingleton;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.job
 * @desc
 * @date 2021/9/18 9:28
 * @Copyright © 河南交科院
 */
@Component
@Slf4j
public class DingDeptJob {
    Logger logger = LoggerFactory.getLogger(DingDeptJob.class);
    @Autowired
    private DeptService service;

    @Value("${dingtalk.token_url}")
    private String tokenUrl;
    @Value("${dingtalk.app_key}")
    private String appKey;
    @Value("${dingtalk.app_secret}")
    private String appsecret;
    @Value("${dingtalk.dept_url}")
    private String deptUrl;

    /**
     * 定时拉取钉钉用户信息
     */
    @Async("syncExecutor")
    @Scheduled(cron="0 */50 * * * ?")
    public void sync(){
        AccessTokenSingleton instance = AccessTokenSingleton.getInstance();
        String token = instance.getToken(tokenUrl, appKey, appsecret);
        List<DingDeptEntity> deptList = null;
        try {
            deptList = instance.getDeptList(deptUrl, token);
        }catch (ApiException e){
            logger.error("获取钉钉单位异常:{}",e);
        }
        //保存数据
        service.saveOrUpdateBatch(deptList);
    }

}
