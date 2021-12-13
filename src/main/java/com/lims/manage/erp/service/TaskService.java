package com.lims.manage.erp.service;

import com.lims.manage.erp.vo.TaskDetailInfoVo;
import com.lims.manage.erp.vo.TaskListParamVo;
import com.lims.manage.erp.vo.TaskListVo;

import java.util.List;

public interface TaskService {

    /**
     * 查询任务详情
     *
     * @return
     */
    List<TaskDetailInfoVo> getTaskDetailInfo(Long taskId);

    /**
     * 查询任务列表
     *
     * @param paramVo
     * @return
     */
    List<TaskListVo> getTaskList(TaskListParamVo paramVo);
}
