package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.vo.SampleItemInstrumentVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/15 15:28
 */
public interface TestDetectionService {

    List<TestInstrumentEntity> getTheInstrument(Integer checkItemId);
    /**
     * 开始试验
     * @param data
     * @return
     */
    Boolean postStartTest(SampleItemInstrumentVo data);
    /**
     * 结束试验
     */
    Boolean postEndTest(SampleItemInstrumentVo data);
    /**
     * 获取任务详情数据 判断任务是否结束
     */
    Boolean JudgmentTaskDetail(TaskDetailInfoVo dataGather,Long TaskId);

    /**
     *  返回 单个 检测项 详情
     * @param dataGather
     * @param TaskId
     * @param itemId
     * @return
     */
    Boolean getTestDetails(TaskDetailInfoVo dataGather,Long TaskId,Integer itemId);
    /**
     * 依据检测项id 变成 复核
     */
    Boolean Postreview(Integer itemId);
}
