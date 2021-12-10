package com.lims.manage.erp.service;

import com.lims.manage.erp.vo.TaskDetailInfoVo;

import java.util.List;

public interface TaskService {

    /**
     * 查询任务列表
     *
     * @return
     */
    List<TaskDetailInfoVo> getTaskDetailInfo();
}
