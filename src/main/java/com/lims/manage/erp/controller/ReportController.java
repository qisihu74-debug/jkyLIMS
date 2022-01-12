package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/report/")
public class ReportController {
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private ReportService reportService;

    @GetMapping("/list")
    public Result getSampleList() {
        return ResultUtil.success("获取可制作报告任务单成功！",reportService.getReportList());
    }

}
