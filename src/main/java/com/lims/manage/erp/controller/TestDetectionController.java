package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestDetectionService;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.SampleItemInstrumentVo;
import com.lims.manage.erp.vo.TaskListParamVo;
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

    @RequestMapping("/getTheInstrument")
    public Result getTheInstrument(Integer checkItemId) {

        List<TestInstrumentEntity> dataCollect = testDetectionService.getTheInstrument(checkItemId);
        if (dataCollect.isEmpty()) {
            return ResultUtil.error(204, "数据为空！");
        }
        return ResultUtil.success(dataCollect);
    }

    @RequestMapping("/start_test")
    public Result PostOnTest(@RequestBody SampleItemInstrumentVo sampleItemInstrumentVo)
    {
         Boolean flag = testDetectionService.PostOnTest(sampleItemInstrumentVo);
        if(flag){
            return ResultUtil.success("成功！！！");
        }
        return ResultUtil.error(204, "失败");
    }

}
