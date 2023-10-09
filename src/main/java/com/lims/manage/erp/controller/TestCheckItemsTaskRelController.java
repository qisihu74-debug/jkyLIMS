package com.lims.manage.erp.controller;


import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.entity.TestCheckItemsTaskRel;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestTaskPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@RestController
@RequestMapping("/testCheckItemsTaskRel")
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
    public Result taskHallDetailsDisplay(Integer taskId, Long entrustId) {
        return testTaskPoolService.taskHallDetailsDisplay(taskId, entrustId);
    }

    /**
     * 任务大厅 领取任务单
     *
     * @param json
     * @return
     */
    @RequestMapping("/addTaskCollection")
    public Result taskCollection(@RequestParam("json") String json) {
        List<TestCheckItemsTaskRel> itemsTaskRels = JSON.parseObject(json, (Type) TestCheckItemsTaskRel.class);
        return testTaskPoolService.addTaskCollection(itemsTaskRels);
    }

}

