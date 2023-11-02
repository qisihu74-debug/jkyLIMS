package com.lims.manage.erp.controller;

import com.lims.manage.erp.http.HttpClientUtil;
import com.lims.manage.erp.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc 契约锁回调类
 * @date 2022/2/28 15:24
 * @Copyright © 河南交科院
 */
@Slf4j
@RestController
@RequestMapping("/qiyuesuo/")
public class QiYueSuoController {
    @Autowired
    private ReportService service;

    /**
     * 契约锁签署报告成功后回调的接口api
     * @return
     */
    @PostMapping("callback")
    public void callback(Long contractId){
        log.debug("接收到契约锁回调请求，合同参数为:{}",contractId);
        //向外业系统转发回执信息
        try {
            HttpClientUtil.get("http://39.98.85.26/jky/wy/qiyuesuo/callback?contractId="+contractId);
        }catch (Exception e){
            log.error("向外业系统转发回执信息失败:{}",e);
        }
        service.callback(contractId);
        log.debug("处理契约锁回调成功！");
    }
}
