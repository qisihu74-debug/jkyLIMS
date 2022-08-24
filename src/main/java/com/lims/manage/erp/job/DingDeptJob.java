package com.lims.manage.erp.job;

import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.mapper.ReportMapper;
import com.lims.manage.erp.service.DeptService;
import com.lims.manage.erp.util.AccessTokenSingleton;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
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
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private QiYueSuoHnadler qiYueSuoHnadler;

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
    @Scheduled(cron="30 10 1 * * ?")
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
    @Async
    @Scheduled(fixedRate = 10000L)
    public void task() {
//        System.out.println("当前线程：" + Thread.currentThread().getName() + " 当前时间" + LocalDateTime.now());
        // 电子印章报告集合
        List<ReportRecordEntity> list = Lists.newArrayList();
        list.addAll(reportMapper.getSealIsNullLastList());
        list.addAll(reportMapper.getSealIsNullMiddleList());
        System.out.println("印章list数据\t"+list);
        if(!CollectionUtils.isEmpty(list)){
            // 获取盖章报告 契约锁合同id contractId
            for(ReportRecordEntity reportRecordEntity:list){
                if(!StringUtils.isEmpty(reportRecordEntity.getContractId())&&
                        StringUtils.isEmpty(reportRecordEntity.getSealUrl())){
                    // 进行填充本地盖章url
                    InputStream inputStream = mehtodSealReport(reportRecordEntity.getContractId(),reportRecordEntity.getReportCode());
                   if(inputStream!=null){
                       // 进行存放至 minIO 服务器
                       long id = GenID.getID();
                       String sealName = MinIoUtil.upload("report-pdf", id + ".pdf", inputStream, "application/pdf");
                       StringBuilder stringBuilder = new StringBuilder();
                       if(!StringUtils.isEmpty(sealName)){
                           String[] fileUrls = sealName.split("\\?");
                           stringBuilder.append(fileUrls[0]);
                       }
                       System.out.println("印章报告zip存储\t"+sealName);
                   }
                }
            }
        }
    }

    /**
     *
     * @param ContractId
     * @return
     */
    private InputStream mehtodSealReport(String ContractId,String reportCode)  {
        byte[] inputStream = qiYueSuoHnadler.downloadQysFile(Long.parseLong(ContractId),
                "郭家林", "18337165257");
        InputStream sbs = new ByteArrayInputStream(inputStream);
        //
        InputStream inputStream1 = null;
        try {
            inputStream1  = FileAndFolderUtil.getZipFileByName(sbs, "签署摘要" + ".pdf");
        }
        catch (Exception e){
            //
        }
        return inputStream1;

    }

}
