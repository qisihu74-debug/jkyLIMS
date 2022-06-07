package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.InstrumentEntity;
import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.service.TestDetectionService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.SampleItemInstrumentVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import com.lims.manage.erp.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 检测项 选择仪器集合
     * @param
     * @return
     */
    @PostMapping("/post_select_instrument")
    public Result postSelectInstrument(@RequestBody InstrumentEntity instrumentEntity){
        // 操作 检测项Id 选择仪器集合 保存
        testDetectionService.postSelectInstrument(instrumentEntity);
        return ResultUtil.success("成功");
    }


    @RequestMapping("/start_test")
    public Result PostOnTest(@RequestBody SampleItemInstrumentVo sampleItemInstrumentVo) {
        // 验证登录人userId 是否具备开始检测资格
        // 验证登录人信息 和部门 存入
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if(testDetectionService.VerifyTheLogin(userInfo.getUserId(),sampleItemInstrumentVo.getTaskId())==false){
            return ResultUtil.error("登录人没有被派发检测资格");
        }
        Boolean flag = testDetectionService.postStartTest(sampleItemInstrumentVo);
        if (flag) {
            return ResultUtil.success("成功！！！");
        }
        return ResultUtil.error(204, "失败");
    }

    /**
     * 开始试验二次开发 废弃
     * @param sampleItemInstrumentVo
     * @return
     */
//    @RequestMapping("/start_test_two")
//    public Result PostOnTestTwo(@RequestBody SampleItemInstrumentVo sampleItemInstrumentVo) {
//        // 验证登录人userId 是否具备开始检测资格
//        // 验证登录人信息 和部门 存入
//        SysUserEntity userInfo = ShiroUtils.getUserInfo();
//        if (userInfo == null) {
//            return ResultUtil.error("token 已过期！");
//        }
//       if(testDetectionService.VerifyTheLogin(userInfo.getUserId(),sampleItemInstrumentVo.getTaskId())==false){
//           return ResultUtil.error("登录人没有被派发检测资格");
//       }
//        Boolean flag = testDetectionService.postStartTest(sampleItemInstrumentVo);
//        if (flag) {
//            return ResultUtil.success("成功！！！");
//        }
//        return ResultUtil.error(204, "失败");
//    }

    /**
     * 结束试验。
     * @param
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

        // 验证登录人userId 是否具备结束检测资格
        // 验证登录人信息 和部门 存入
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if(testDetectionService.VerifyTheLogin(userInfo.getUserId(),sampleItemInstrumentVo.getTaskId())==false){
            return ResultUtil.error("登录人没有被派发检测资格");
        }

        Boolean flag = testDetectionService.postEndTest(sampleItemInstrumentVo);
        if(flag) {
            // 更新任务单状态 需要 对所有的 样品信息 下 检测项 进行判断 ==2的话 更新。
            TaskDetailInfoVo dataGather = taskService.getTaskDetailInfoTwo(sampleItemInstrumentVo.getTaskId(),null);
            Boolean DetailStatus = testDetectionService.JudgmentTaskDetail(dataGather, sampleItemInstrumentVo.getTaskId());
            if (DetailStatus==true) {
                return ResultUtil.success("任务单完成！！！");
            }
            return ResultUtil.success("检测项未全部完成检测，任务单未结束","整体任务单未结束");
        }
        return ResultUtil.error(204, "缺少必要参数未上传！！");
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
