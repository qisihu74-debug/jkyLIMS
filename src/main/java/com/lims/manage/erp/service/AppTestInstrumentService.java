package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.InstrumentUseGroup;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.*;

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
//    String startToTest(InstrumentVo instrumentVo);
    Result startToTest(InstrumentVo instrumentVo);

    /**
     * 开始试验--正常开始--生成记录
     * @param instrumentVo
     * @return
     */
    Result startToTestNew(InstrumentParamVo instrumentVo);
    /**
     * 开始试验--正常开始--不生成记录
     * @param instrumentVo
     * @return
     */
    Result startToTestNewNo(InstrumentParamVo instrumentVo);

    /**
     * 开始试验--插单
     * @param instrumentVo
     * @return
     */
    Result startToTestNewInsert(InstrumentParamVo instrumentVo);

    /**
     * 创建组队信息
     * @param instrumentVo
     * @return
     */
    Result createGroup(InstrumentParamVo instrumentVo);

    /**
     * 退出队伍
     * @param group
     * @return
     */
    Result deleteGroup(InstrumentUseGroup group);


    /**
     * 结束试验
     * @param instrumentVo 数据源
     * @param type 类型 （结束试验的话 type = 1、点击提交复核 type =2）
     * @return
     */
//    String endToTest(InstrumentVo instrumentVo,Integer type);
    Result endToTest(InstrumentVo instrumentVo,Integer type);
    Result endToTestNew(InstrumentVo instrumentVo);

    /**
     * 根据设备id 查询详情数据
     */
    InstrumentAppVo InstrumentDetails(Long id);
    /**
     * 根据设备id 查询设备详情
     */
    InstrumentAppVo getDetailsNew(Long id);

    /**
     * 查询设备
     * @param instrumentVo
     * @return
     */
    List<InstrumentRecordVo> getInstrumentUseTime(InstrumentVo instrumentVo);

    /**
     * 根据记录id返回记录详细信息
     */
    InstrumentAppVo getRecordDetails(Long id);



}
