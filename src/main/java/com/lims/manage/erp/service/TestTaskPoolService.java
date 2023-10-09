package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.TestTaskPool;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.result.Result;

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
     * @param taskId
     * @return
     */
    Result taskHallDetailsDisplay(Integer taskId,Long entrustId);

}
