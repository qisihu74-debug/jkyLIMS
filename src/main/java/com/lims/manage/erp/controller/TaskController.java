package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/task/")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @RequestMapping("/getTaskInfo")
    public Result getTaskInfo() {
        return ResultUtil.success(taskService.getTaskDetailInfo());
    }
}
