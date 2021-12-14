package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TaskTestTeamEntity;
import com.lims.manage.erp.vo.LabelValueTeamVo;
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
    TaskDetailInfoVo getTaskDetailInfo(Long taskId);

    /**
     * 查询任务列表
     *
     * @param paramVo
     * @return
     */
    List<TaskListVo> getTaskList(TaskListParamVo paramVo);

//    List<TaskDetailInfoVo> getTaskDetailInfo();
    /**
     * 副团长抢单并 派发 团队人员 操作
     */
    Boolean postGrabASingle(TaskTestEntity taskTestEntity);
    /**
     * 返回 团队成员姓名
     */
    List<LabelValueTeamVo> getTeamUserName(Long UserLong);
}
