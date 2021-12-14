package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TaskTestTeamEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.vo.LabelValueTeamVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import com.lims.manage.erp.vo.TaskListParamVo;
import com.lims.manage.erp.vo.TaskListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;

    @Override
    public TaskDetailInfoVo getTaskDetailInfo(Long taskId) {
        return taskMapper.getTaskDetailInfo(taskId);
    }

    @Override
    public List<TaskListVo> getTaskList(TaskListParamVo paramVo) {
        return taskMapper.getTaskList(paramVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postGrabASingle(TaskTestEntity taskTestEntity) {
        // 抢单
        taskTestEntity.setState(1);
        // 根据角色查询团队名
        if(taskTestEntity.getReceiver()!=null){
            // 任务编号 团队名称+编号=任务编号
            TaskTestTeamEntity dataTeam = taskMapper.selectTeamCode(Long.parseLong(taskTestEntity.getReceiver()));
            if(dataTeam==null){
                return false;
            }
            taskTestEntity.setTaskCode(dataTeam.getCode()+taskTestEntity.getCode());
            taskTestEntity.setTeamId(String.valueOf(dataTeam.getId()));
        }
        // 抢单时间
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        taskTestEntity.setReceiveTime(currentDate);
        taskMapper.updateTestTask(taskTestEntity);
        return true;
    }
    @Override
    public List<LabelValueTeamVo> getTeamUserName(Long UserLong) {
        TaskTestTeamEntity dataTeam = taskMapper.selectTeamCode(UserLong);
        if(dataTeam!=null){
             return taskMapper.selectTeamList(dataTeam.getId());
        }
        return null;
    }
}
