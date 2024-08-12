package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.TestCheckItemsTaskRel;
import com.lims.manage.erp.entity.TestTaskPool;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * <p>
 * 任务单 服务类
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
public interface TestTaskPoolService extends IService<TestTaskPool> {
    /**
     * 任务大厅 展示详情数据
     *
     * @param poolId
     * @return
     */
    Result taskHallDetailsDisplay(Long poolId, Long entrustId);

    /**
     * 任务大厅 - 根据登录人、返回所属团队成员的对应检测项。
     *
     * @param poolId
     * @param entrustId
     * @return
     */
    Result getTaskDetectionItemDetails(Long poolId, Long entrustId);

    /**
     * 任务大厅 领取任务单
     *
     * @param list
     * @return
     */
    Result addTaskCollection(List<SampleItemEntity> list);

    /**
     * 试验检测：任务单判断是否为 new创建。
     * 是：则 判断当前检测项 是否能够操作。
     *
     * @param taskId
     * @param items
     * @return
     */
    Result testDetectionTasks(Long taskId, List<Integer> items, Integer type);


}
