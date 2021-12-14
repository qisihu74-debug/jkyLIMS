package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TaskTestTeamEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.vo.ReceiveSampleListVo;
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
    public List<ReceiveSampleListVo> getSampleList(TaskListParamVo paramVo) {
        String receiveTime = paramVo.getReceiveTime();
        if (receiveTime != null) {
            String[] split = receiveTime.split("~");
            paramVo.setBeginDate(split[0]);
            paramVo.setEndDate(split[1]);
        }
        return taskMapper.getSampleList(paramVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postGrabASingle(TaskTestEntity taskTestEntity) {
        // 抢单
        taskTestEntity.setState(1);
        // 根据角色查询团队名
        if (taskTestEntity.getReceiver() != null) {
            // 任务编号 团队名称+编号=任务编号
            TaskTestTeamEntity dataTeam = taskMapper.selectTeamCode(Long.parseLong(taskTestEntity.getReceiver()));
            if (dataTeam == null) {
                return false;
            }
            String strDate = taskTestEntity.getCode();
            String str1 = strDate.substring(0, 4);
            String str2 = strDate.substring(strDate.length() - 3);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str1);
            stringBuilder.append("-");
            stringBuilder.append(str2);
            taskTestEntity.setTaskCode(dataTeam.getCode() + stringBuilder);
            taskTestEntity.setTeamId(String.valueOf(dataTeam.getId()));
            // 抢单时间
            java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
            taskTestEntity.setReceiveTime(currentDate);
            taskMapper.updateTestTask(taskTestEntity);
            return true;
        }
        return false;
    }

    @Override
    public List<LabelValueTeamVo> getTeamUserName(Long UserLong) {
        TaskTestTeamEntity dataTeam = taskMapper.selectTeamCode(UserLong);
        if (dataTeam != null) {
            return taskMapper.selectTeamList(dataTeam.getId());
        }
        return null;
    }

    @Override
    public Boolean getJudgmentTaskList(Long id) {
        if (taskMapper.getJudgmentTaskList(id) == 0) {
            return true;
        }
        return false;
    }
}
