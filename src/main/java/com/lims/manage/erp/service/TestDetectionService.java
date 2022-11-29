package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.InstrumentEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.vo.SampleItemInstrumentVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import com.lims.manage.erp.vo.DeviceUseTimeVo;

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
     *  操作 检测项Id 选择仪器集合 保存
     */
    Boolean postSelectInstrument(InstrumentEntity instrumentEntity);

    /**
     * 验证登录人userId 是否具备开始检测资格
     * @param userId
     * @param taskId
     * @return true 具备 false 不具备
     */
    Boolean VerifyTheLogin(Long userId,Long taskId);

    /**
     * 验证登录人userId 是否具备开始复核资格
     * @param userId
     * @param taskId
     * @return
     */
    Boolean reviewTheLogin(Long userId,Long taskId);
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

    /**
     * 校验设备使用时间
     * @param vo
     * @return
     */
    DeviceUseTimeVo checkDeviceUseTime(DeviceUseTimeVo vo);
}
