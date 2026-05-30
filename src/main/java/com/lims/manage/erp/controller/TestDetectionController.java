package com.lims.manage.erp.controller;

import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.InstrumentEntity;
import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.SampleItemInstrumentVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import com.lims.manage.erp.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
    @Autowired
    private PageOfficeCopyService pageOfficeCopyService;
    @Autowired
    private TestProductItemDao testProductItemDao;
    @Resource
    private TestCheckItemsTaskRelService testCheckItemsTaskRelService;
    @Resource
    private TestTaskPoolService testTaskPoolService;

    @RequestMapping("/getTheInstrument")
    public Result getTheInstrument(Integer escRelId, Integer checkItemId) {
        List<TestInstrumentEntity> dataCollect = testDetectionService.getTheInstrument(escRelId, checkItemId);
        if (dataCollect.isEmpty()) {
            return ResultUtil.error(204, "数据为空！");
        }
        return ResultUtil.success(dataCollect);
    }

    /**
     * 查询设备使用人下拉列表
     * @return
     */
    @RequestMapping("getDeviceUser")
    public Result getDeviceUser() {
        if (ShiroUtils.getUserInfo() != null) {
            //设备使用人
            List<LabelValueVo> deviceUser = taskService.getDeviceUser(ShiroUtils.getUserInfo().getUserId());
            return ResultUtil.success(deviceUser);
        }
        return ResultUtil.error(502, "token过期！");
    }


    /**
     * 检测项 选择仪器集合
     * @param
     * @return
     */
    @PostMapping("/post_select_instrument")
    public Result postSelectInstrument(@RequestBody InstrumentEntity instrumentEntity){
        // 操作 检测项Id 选择仪器集合 保存
        testDetectionService.postSelectInstrument0328(instrumentEntity);
        return ResultUtil.success("选择仪器成功！");
    }


    @RequestMapping("/start_test")
    public Result PostOnTest(@RequestBody SampleItemInstrumentVo sampleItemInstrumentVo) {
        // 验证登录人userId 是否具备开始检测资格
        // 验证登录人信息 和部门 存入
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        // 委托单144 则不能执行任务单
        if (taskService.judgeTaskStatus(sampleItemInstrumentVo.getTaskId())) {
            return ResultUtil.error(678, "开始试验失败！任务单已废弃！！！");
        }
        if (testDetectionService.VerifyTheLogin(userInfo.getUserId(), sampleItemInstrumentVo.getTaskId()) == false) {
            return ResultUtil.error("登录人没有被派发检测资格");
        }
        List<Integer> items = new ArrayList<>();
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : sampleItemInstrumentVo.getItemInstrumentEntityList()) {
            items.add(sampleItemInstrumentEntity.getItemId());
        }
        Result msg = testTaskPoolService.testDetectionTasks(sampleItemInstrumentVo.getTaskId(), items, 0);
        if (msg.getCode() == null) {
            return msg;
        }
        Boolean flag = testDetectionService.postStartTest(sampleItemInstrumentVo);
        if (flag) {
            return ResultUtil.success("开始试验成功！！！");
        }
        return ResultUtil.error(204, "开始试验失败！！！");
    }

    /**
     * 结束试验。
     * @param
     * @return
     */
    @RequestMapping("/end_test")
    public Result PostEndTest1(@RequestBody EndTestParamVo paramVo) throws Exception {
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
        // 委托单144 则不能执行任务单
        if (taskService.judgeTaskStatus(sampleItemInstrumentVo.getTaskId())) {
            return ResultUtil.error(678, "结束试验失败！任务单已废弃！！！");
        }
        if (testDetectionService.VerifyTheLogin(userInfo.getUserId(), sampleItemInstrumentVo.getTaskId()) == false) {
            return ResultUtil.error("结束试验只能由检测人操作");
        }
        List<Integer> items = new ArrayList<>();
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : sampleItemInstrumentVo.getItemInstrumentEntityList()) {
            items.add(sampleItemInstrumentEntity.getItemId());
        }
        Result msg0 = testTaskPoolService.testDetectionTasks(sampleItemInstrumentVo.getTaskId(), items, 0);
        if (msg0.getCode() == null) {
            return msg0;
        }
        // 比较检测项 start_time 与 end_time 时间
        String msg = testDetectionService.compareItemTime(sampleItemInstrumentVo);
        if (msg != null) {
            return ResultUtil.error(msg);
        }
        // 通过检测项主键验证签名信息
        String str = testDetectionService.personnelComparison(sampleItemInstrumentVo);
        if (str != null) {
            return ResultUtil.error(str);
        }
        Boolean flag = testDetectionService.postEndTest(sampleItemInstrumentVo);
        try {
            // 每组检测项统计信息 并进行试验
            pageOfficeCopyService.updateItemOriginUr(paramVo);
            // 试验完成 对检测项下 含有对应的 excel 转成pdf 进行更新origin_url_pdf。
            List<SampleItemInstrumentEntity> sampleItemInstrumentEntities = new ArrayList<>();
            for (Integer itemId : paramVo.getItemInstrumentEntityList()) {
                SampleItemInstrumentEntity data = new SampleItemInstrumentEntity();
                data.setItemId(itemId);
                sampleItemInstrumentEntities.add(data);
            }
            sampleItemInstrumentVo.setItemInstrumentEntityList(sampleItemInstrumentEntities);
            pageOfficeCopyService.updateItemOriginUrlPdf(sampleItemInstrumentVo);
        } catch (Exception e) {
            System.out.println("编辑原始记录异常抛出");
            e.printStackTrace();
        }
        if (flag) {
            // 更新任务单状态 需要 对所有的 样品信息 下 检测项 进行判断 ==2的话 更新。
            TaskDetailInfoVo dataGather = taskService.getTaskDetailInfoTwo(sampleItemInstrumentVo.getTaskId(), null);
            testDetectionService.JudgmentTaskDetail(dataGather, sampleItemInstrumentVo.getTaskId());
            return ResultUtil.success("检测项未全部完成检测，任务单未结束", "整体任务单未结束");
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

    @RequestMapping("/checkDeviceUseTime")
    public Result checkDeviceUseTime(@RequestBody DeviceUseTimeVo vo) {
        if (vo == null || vo.getStartTime() == null || vo.getEndTime() == null || vo.getDeviceId() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        DeviceUseTimeVo vo1 = testDetectionService.checkDeviceUseTime(vo);
        return ResultUtil.success(vo1);
    }
}
