package com.lims.manage.erp.controller;


import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.service.TestTaskPoolService;
import com.lims.manage.erp.vo.EntrustAddVo;
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
     * @param poolId
     * @param entrustId
     * @return
     */
    @GetMapping("/taskHallDetailsDisplay")
    public Result taskHallDetailsDisplay(Long poolId, Long entrustId) {
        return testTaskPoolService.taskHallDetailsDisplay(poolId, entrustId);
    }

    /**
     * 任务大厅 领取任务单
     *
     * @param list
     * @return
     */
    @RequestMapping("/taskCollection")
//    public Result taskCollection(@RequestBody List<SampleItemEntity> list) {
    public Result taskCollection(@RequestParam("json") String json) {
        System.out.println("展示数据 ");
//        List<SampleItemEntity> list  = JSON.parseObject(json, (Type) SampleItemEntity.class);
        List<SampleItemEntity> list = JSON.parseArray(json, SampleItemEntity.class);
        return testTaskPoolService.addTaskCollection(list);
    }

}

