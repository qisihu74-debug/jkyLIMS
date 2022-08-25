package com.lims.manage.erp.job;

import com.github.pagehelper.PageHelper;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.ReportMapper;
import com.lims.manage.erp.service.DeptService;
import com.lims.manage.erp.util.AccessTokenSingleton;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.ReportSealvVo;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
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
import java.io.IOException;
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
    @Scheduled(cron = "0 0 * * * ?")
    public void task() throws IOException {
        // 电子印章报告集合
        List<ReportSealvVo> list = Lists.newArrayList();
        PageHelper.clearPage();
        list.addAll(reportMapper.getSealIsNullLastList());
        PageHelper.clearPage();
        list.addAll(reportMapper.getSealIsNullMiddleList());
        logger.info("印章list数据\t"+list);
        if(!CollectionUtils.isEmpty(list)){
            // 获取盖章报告 契约锁合同id contractId
            for(ReportSealvVo reportRecordEntity:list){
                if(!StringUtils.isEmpty(reportRecordEntity.getContractId())&&
                        StringUtils.isEmpty(reportRecordEntity.getSealReportUrl())){
                    // 根据报告提交申请人 查询手机号
                    PageHelper.clearPage();
                    SysUserEntity sysUserEntity = reportMapper.selectUserMobile(reportRecordEntity.getApplicant());
                    // 进行填充本地盖章url
                    InputStream inputStream = mehtodSealReport(reportRecordEntity.getContractId(),
                            reportRecordEntity.getReportCode(),sysUserEntity.getName(),sysUserEntity.getMobile());
                   if(inputStream.available()!=0){
                       // 进行存放至 minIO 服务器
                       long id = GenID.getID();
                       String sealName = MinIoUtil.upload("report-pdf", id + ".pdf", inputStream, "application/pdf");
                       StringBuilder stringBuilder = new StringBuilder();
                       if(!StringUtils.isEmpty(sealName)){
                           String[] fileUrls = sealName.split("\\?");
                           stringBuilder.append(fileUrls[0]);
                       }
                       logger.info("报告id\t"+reportRecordEntity.getId()+"印章报告zip存储\t"+sealName+
                               "报告编号\t"+reportRecordEntity.getReportCode());
                       // update 更新中间报告数据
                       reportRecordEntity.setSealReportUrl(sealName);
                       if(reportRecordEntity.getType().equals(0)){
                           reportMapper.updateReportSealMid(reportRecordEntity);
                       }
                       // update 最终报告数据
                       if(reportRecordEntity.getType().equals(1)){
                           reportMapper.updateReportSealLast(reportRecordEntity);
                       }
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
    private InputStream mehtodSealReport(Long ContractId,String reportCode,String name,String mobile)  {
        byte[] inputStream = qiYueSuoHnadler.downloadQysFile(ContractId, name, mobile);
        InputStream sbs = new ByteArrayInputStream(inputStream);
        InputStream inputStream1 = null;
        try {
            inputStream1  = FileAndFolderUtil.getZipFileByName(sbs, reportCode + ".pdf");
        }
        catch (Exception e){
        logger.error(String.valueOf(e));
        inputStream1 = null;
        }
        return inputStream1;

    }

}
