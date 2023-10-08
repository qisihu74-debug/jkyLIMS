package com.lims.manage.erp.controller;


import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestTaskPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@RestController
@RequestMapping("/test-check-items-task-rel")
public class TestCheckItemsTaskRelController {

    @Autowired
    private TestTaskPoolService testTaskPoolService;

    /**
     * 任务大厅 展示详情数据
     *
     * @param taskId
     * @return
     */
    @GetMapping("/taskHallDetailsDisplay")
    public Result taskHallDetailsDisplay(Integer taskId,Long entrustId) {
        return testTaskPoolService.taskHallDetailsDisplay(taskId,entrustId);
    }

}

