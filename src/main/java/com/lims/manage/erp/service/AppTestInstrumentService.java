package com.lims.manage.erp.service;

import com.lims.manage.erp.vo.InstrumentVo;
import com.lims.manage.erp.vo.LabelValueTeamVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TaskListVo;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2023/4/3 15:37
 */
public interface AppTestInstrumentService {

    /**
     * 新增检测任务列表 (根据检测人id 返回待任务单检测列表)
     * @param search
     * @param userId
     * @return
     */
    List<TaskListVo> detectionTaskList(String search,Long userId);

    /**
     * 当前任务列表 (根据设备id 返回列表)
     * @param search
     * @param instrumentId
     * @return
     */
    List<TaskListVo> taskList(String search,Long instrumentId);

    /**
     * 返回团队人员信息列表
     * @param userId
     * @return
     */
    List<LabelValueVo> returnPersonList(Long userId);

    /**
     * 开始试验
     * @param instrumentVo
     * @return
     */
    String startToTest(InstrumentVo instrumentVo);

    /**
     * 结束试验
     * @param instrumentVo 数据源
     * @param type 类型 （结束试验的话 type = 1、点击提交复核 type =2）
     * @return
     */
    String endToTest(InstrumentVo instrumentVo,Integer type);

}
