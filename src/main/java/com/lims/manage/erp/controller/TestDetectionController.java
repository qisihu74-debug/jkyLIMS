package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.service.TestDetectionService;
import com.lims.manage.erp.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/15 15:16
 */
@RestController
@RequestMapping("/test/")
public class TestDetectionController {

    @Autowired
    TestDetectionService testDetectionService;
    @Autowired
    private TaskService taskService;

    @RequestMapping("/getTheInstrument")
    public Result getTheInstrument(Integer checkItemId) {
        List<TestInstrumentEntity> dataCollect = testDetectionService.getTheInstrument(checkItemId);
        if (dataCollect.isEmpty()) {
            return ResultUtil.error(204, "数据为空！");
        }
        return ResultUtil.success(dataCollect);
    }

    @RequestMapping("/start_test")
    public Result PostOnTest(@RequestBody SampleItemInstrumentVo sampleItemInstrumentVo) {
        Boolean flag = testDetectionService.PostOnTest(sampleItemInstrumentVo);
        if (flag) {
            return ResultUtil.success("成功！！！");
        }
        return ResultUtil.error(204, "失败");
    }

    /**
     * 结束试验。
     *
     * @param sampleItemInstrumentVo
     * @return
     */
    @RequestMapping("/end_test")
//    public Result PostEndTest1(SampleItemInstrumentVo sampleItemInstrumentVo) {
    public Result PostEndTest1(@RequestBody EndTestParamVo paramVo) {
        SampleItemInstrumentVo sampleItemInstrumentVo = new SampleItemInstrumentVo();
        sampleItemInstrumentVo.setResult(paramVo.getResult());
        sampleItemInstrumentVo.setEndTime(paramVo.getEndTime());
        sampleItemInstrumentVo.setTaskId(paramVo.getTaskId());
        List<SampleItemInstrumentEntity> list = Lists.newArrayList();
        for (int i = 0; i < paramVo.getItemInstrumentEntityList().size(); i++) {
            SampleItemInstrumentEntity e = new SampleItemInstrumentEntity();
            e.setItemId(paramVo.getItemInstrumentEntityList().get(i));
            list.add(e);
        }
        sampleItemInstrumentVo.setItemInstrumentEntityList(list);

        Boolean flag = testDetectionService.PostEndTest1(sampleItemInstrumentVo);
        if(flag) {
            // 更新任务单状态 需要 对所有的 样品信息 下 检测项 进行判断 ==2的话 更新。
            TaskDetailInfoVo dataGather = taskService.getTaskDetailInfo(sampleItemInstrumentVo.getTaskId());
            Boolean DetailStatus = testDetectionService.JudgmentTaskDetail(dataGather, sampleItemInstrumentVo.getTaskId());
            if (DetailStatus) {
                return ResultUtil.success("任务单完成！！！");
            }
            return ResultUtil.success("缺少必要参数未上传！！！","整体任务单未结束");
        }
        return ResultUtil.error(204, "检测项状态改变失败");
    }
    /**
     *  依据检测项id 复核
     */
    @RequestMapping("/review")
    public Result Postreview(Integer itemId)
    {
        Boolean DetailStatus = testDetectionService.Postreview(itemId);
        if(DetailStatus){
            return ResultUtil.success("修改完成！！！");
        }
        return ResultUtil.error(204, "检测项状态改变失败");
    }




}
