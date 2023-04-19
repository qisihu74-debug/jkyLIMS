package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.entity.ReportOriginalEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportOriginalService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 报告，原始记录
 */
@Slf4j
@RestController
@RequestMapping("/reportOriginal/")
public class ReportOriginalController {
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private ReportOriginalService reportOriginalService;

    @PostMapping("/addReportOriginal")
    public Result addReportOriginal(@RequestParam("json") String json, MultipartFile file) {
        if (file == null) {
            return ResultUtil.error("报告模板不能为空！");
        }
        ReportOriginalEntity reportOriginalEntity = JSON.parseObject(json, ReportOriginalEntity.class);
        if(reportOriginalEntity.getCode() == null || reportOriginalEntity.getName() == null){
            return ResultUtil.error("缺少必要参数！");
        }
        int i = reportOriginalService.addReportOriginal(reportOriginalEntity, file);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(i>0){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                    +"新增报告原始记录模板成功！", Const.REPORT_ORIGINAL,true);
            return ResultUtil.success("新增报告原始记录模板成功!",i);
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                +"新增报告原始记录模板失败！", Const.REPORT_ORIGINAL,false);
        return ResultUtil.error("新增报告原始记录模板失败");
    }
}
