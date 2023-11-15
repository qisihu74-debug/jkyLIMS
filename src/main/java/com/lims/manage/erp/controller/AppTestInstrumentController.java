package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.InstrumentUseGroup;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.AppTestInstrumentService;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    public Result detectionTaskList(String search, Long instrumentId) {
        // 当前任务单列表 == null ，调用检测任务列表
        if (CollectionUtils.isEmpty(appTestInstrumentService.taskList(search, instrumentId))) {
            // 验证登录人信息 和部门 存入
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            return ResultUtil.success(appTestInstrumentService.detectionTaskList(search, userInfo.getUserId()));
        }
        // 非空 返回空集合
        return ResultUtil.error("当前任务列表未全部结束");
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
     * 任务单详情（检测项复核通过不展示）
     *
     * @param taskIds
     * @return
     */
    @RequestMapping("taskDetails")
    @ResponseBody
    public Result taskDetails(@RequestParam List<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        List<TaskDetailInfoVo> list = new ArrayList<>();
        for (Long taskId : taskIds) {
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
                                if (checkItemVo.getEndTime() != null && checkItemVo.getState() == 3) {
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            }
            list.add(taskDetails);
        }
        return ResultUtil.success("查询任务详情成功！", list);
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
    @RequestMapping("startToTestOld")
    public Result startToTest(@RequestBody InstrumentVo instrumentVo) {
        if (instrumentVo == null) {
            return ResultUtil.error("参数不能为空");
        }
        if (CollectionUtils.isEmpty(instrumentVo.getCheckItemInfoList())) {
            return ResultUtil.error("参数不能为空");
        }
        return appTestInstrumentService.startToTest(instrumentVo);
    }

    /**
     * 新APP开始试验
     * @param instrumentVo
     * @return
     */
    @RequestMapping("startToTest")
    public Result startToTestNew(@RequestBody InstrumentParamVo instrumentVo) {
//        if (instrumentVo == null || instrumentVo.getInstrumentVoList().get(0).getRecordType() == null) {
//            return ResultUtil.error("参数不能为空");
//        }
        if(instrumentVo.getInsertType() != null){//插单
            return appTestInstrumentService.startToTestNewInsert(instrumentVo);
        }else{//正常记录
            if(instrumentVo.getIsShow() != null && instrumentVo.getIsShow().equals(0)){//生成记录
                return appTestInstrumentService.startToTestNew(instrumentVo);
            }else{//不生成记录
                return appTestInstrumentService.startToTestNewNo(instrumentVo);
            }
        }
    }

    /**
     * 创建队伍
     * @param instrumentVo
     * @return
     */
    @RequestMapping("createGroup")
    public Result createGroup(@RequestBody InstrumentParamVo instrumentVo) {
        if (instrumentVo == null) {
            return ResultUtil.error("参数不能为空");
        }
        return appTestInstrumentService.createGroup(instrumentVo);
    }

    /**
     * 退出队伍
     * @param group
     * @return
     */
    @RequestMapping("deleteGroup")
    public Result deleteGroup(@RequestBody InstrumentUseGroup group) {
        return appTestInstrumentService.deleteGroup(group);
    }

    /**
     * 结束试验
     *
     * @param instrumentVo
     * @return
     */
    @RequestMapping("endToTestOld")
    public Result endToTest(@RequestBody InstrumentVo instrumentVo) {
        if (instrumentVo == null) {
            return ResultUtil.error("参数不能为空");
        }
        if (CollectionUtils.isEmpty(instrumentVo.getInstrumentRecordListVos())) {
            return ResultUtil.error("参数不能为空");
        }
        return appTestInstrumentService.endToTest(instrumentVo, 2);
    }

    /**
     * 新版APP结束试验
     * @param instrumentVo
     * @return
     */
    @RequestMapping("endToTest")
    public Result endToTestNew(@RequestBody InstrumentVo instrumentVo) {
        if (instrumentVo == null) {
            return ResultUtil.error("参数不能为空");
        }
        return appTestInstrumentService.endToTestNew(instrumentVo);
    }

    /**
     * 结束复核
     *
     * @param instrumentVo
     * @return
     */
    @RequestMapping("closingReview")
    public Result closingReview(@RequestBody InstrumentVo instrumentVo) {
        if (instrumentVo == null) {
            return ResultUtil.error("参数不能为空");
        }
        if (CollectionUtils.isEmpty(instrumentVo.getInstrumentRecordListVos())) {
            return ResultUtil.error("参数不能为空");
        }
        return ResultUtil.success(appTestInstrumentService.endToTest(instrumentVo, 2));
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

    @RequestMapping("getDetailsNew")
    public Result getDetailsNew(Long instrumentId) {
        if (instrumentId == null || instrumentId.equals("")) {
            return ResultUtil.error("参数为空");
        }
        return ResultUtil.success(appTestInstrumentService.getDetailsNew(instrumentId));
    }

    /**
     * 查询指定时间段内可用时间
     * @param instrumentVo
     * @return
     */
    @RequestMapping("getInstrumentUseTime")
    public Result getInstrumentUseTime(@RequestBody InstrumentVo instrumentVo) {
        if (instrumentVo == null || instrumentVo.getStartTime() == null
                || instrumentVo.getEndTime() == null || instrumentVo.getId() == null) {
            return ResultUtil.error("参数为空！");
        }
        return ResultUtil.success(appTestInstrumentService.getInstrumentUseTime(instrumentVo));
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
