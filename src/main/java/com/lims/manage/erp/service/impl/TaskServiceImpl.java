package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import com.lims.manage.erp.vo.TaskListParamVo;
import com.lims.manage.erp.vo.TaskListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;

    @Override
    public List<TaskDetailInfoVo> getTaskDetailInfo(Long taskId) {
        return taskMapper.getTaskDetailInfo(taskId);
    }

    @Override
    public List<TaskListVo> getTaskList(TaskListParamVo paramVo) {
        return taskMapper.getTaskList(paramVo);
    }
}
