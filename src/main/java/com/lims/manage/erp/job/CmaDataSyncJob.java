package com.lims.manage.erp.job;

import com.lims.manage.erp.service.CmaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CmaDataSyncJob {

    private static final Logger logger = LoggerFactory.getLogger(CmaDataSyncJob.class);

    @Resource
    private CmaService cmaService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void syncDaily() {
        logger.info("CMA定时同步任务启动...");
        cmaService.syncFromCma();
        logger.info("CMA定时同步任务完成");
    }
}
