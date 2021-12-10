package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;

    @Override
    public List<TaskDetailInfoVo> getTaskDetailInfo() {
        return taskMapper.getTaskDetailInfo();
    }
}
