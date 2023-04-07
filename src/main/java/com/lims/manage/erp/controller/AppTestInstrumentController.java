package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.AppTestInstrumentService;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import com.lims.manage.erp.vo.InstrumentVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Iterator;

/**
 * @Author: DLC
 * @Date: 2023/4/3 15:09
 */
@Slf4j
@RestController
@RequestMapping("/app/testInstrument/")
public class AppTestInstrumentController {

    @Resource
    AppTestInstrumentService appTestInstrumentService;
    @Autowired
    private TaskService taskService;

    /**
     * 新增检测任务列表 (根据检测人id 返回待任务单检测列表)
     *
     * @param search
     * @return
     */
    @RequestMapping("detectionTaskList")
    public Result detectionTaskList(String search) {
        // 验证登录人信息 和部门 存入
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        return ResultUtil.success(appTestInstrumentService.detectionTaskList(search, userInfo.getUserId()));
    }

    /**
     * 当前任务列表 (根据设备id 返回列表)
     *
     * @param search
     * @param instrumentId
     * @return
     */
    @RequestMapping("taskList")
    public Result taskList(String search,Long instrumentId) {
        return ResultUtil.success(appTestInstrumentService.taskList(search, instrumentId));
    }

    /**
     * 任务单详情（检测项待复核不展示）
     *
     * @param taskId
     * @return
     */
    @RequestMapping("taskDetails")
    public Result taskDetails(Long taskId) {
        if (StringUtils.isEmpty(taskId)) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        // 根据 taskId 展示详情
        TaskDetailInfoVo taskDetails = taskService.getTaskDetailInfoTwo(taskId, null);
        if (taskDetails != null) {
            // 遍历样品
            if (!CollectionUtils.isEmpty(taskDetails.getSampleDetailList())) {
                for (SampleDetailVo sampleDetailVo : taskDetails.getSampleDetailList()) {
                    // 处理 检测项待复核 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回
                    if (!CollectionUtils.isEmpty(sampleDetailVo.getCheckItemInfoList())) {
                        Iterator<CheckItemInfoVo> it = sampleDetailVo.getCheckItemInfoList().iterator();
                        while (it.hasNext()) {
                            CheckItemInfoVo checkItemVo = it.next();
                            if (checkItemVo.getEndTime() != null && (checkItemVo.getState() >= 2 || checkItemVo.getState() >= 3)) {
                                it.remove();
                            }
                        }
                    }
                }
            }
        }
        return ResultUtil.success("查询任务详情成功！", taskDetails);
    }

    /**
     * 返回团队人员信息列表
     *
     * @return
     */
    @RequestMapping("returnPersonList")
    public Result returnPersonList() {
        // 验证登录人信息 和部门 存入
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        return ResultUtil.success(appTestInstrumentService.returnPersonList(userInfo.getUserId()));
    }

    /**
     * 开始试验
     * @param instrumentVo
     * @return
     */
    @RequestMapping("startToTest")
    public Result startToTest(@RequestBody InstrumentVo instrumentVo) {
        return ResultUtil.success(appTestInstrumentService.startToTest(instrumentVo));
    }

    /**
     * 结束试验
     *
     * @param instrumentVo
     * @return
     */
    @RequestMapping("endToTest")
    public Result endToTest(@RequestBody InstrumentVo instrumentVo) {
        return ResultUtil.success(appTestInstrumentService.endToTest(instrumentVo,1));
    }

    /**
     * 结束复核
     *
     * @param instrumentVo
     * @return
     */
    @RequestMapping("closingReview")
    public Result closingReview(@RequestBody InstrumentVo instrumentVo) {
        return ResultUtil.success(appTestInstrumentService.endToTest(instrumentVo,2));
    }

    /**
     * 根据设备id查询设备详细信息
     *
     * @param instrumentId
     * @return
     */
    @RequestMapping("getDetails")
    public Result InstrumentDetails(Long instrumentId) {
        if (instrumentId == null || instrumentId.equals("")) {
            return ResultUtil.error("参数为空");
        }
        return ResultUtil.success(appTestInstrumentService.InstrumentDetails(instrumentId));
    }

    /**
     * 根据记录id返回记录详细信息
     *
     * @param recordId
     * @return
     */
    @RequestMapping("getRecordDetails")
    public Result getRecordDetails(Long recordId) {
        if (recordId == null || recordId.equals("")) {
            return ResultUtil.error("参数为空");
        }
        return ResultUtil.success(appTestInstrumentService.getRecordDetails(recordId));
    }


}
