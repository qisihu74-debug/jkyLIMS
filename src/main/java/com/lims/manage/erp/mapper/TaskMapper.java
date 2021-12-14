package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TaskEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TaskTestTeamEntity;
import com.lims.manage.erp.vo.ReceiveSampleListVo;
import com.lims.manage.erp.vo.LabelValueTeamVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import org.apache.ibatis.annotations.Mapper;
import com.lims.manage.erp.vo.TaskListParamVo;
import com.lims.manage.erp.vo.TaskListVo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/12/6 17:47
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface TaskMapper extends BaseMapper {

    /**
     * 获取最大的任务编号
     *
     * @return
     */
    Integer selectMaxNo();

    /**
     * 更新委托单状态
     *
     * @param entrustmentId
     */
    void updateEntrustById(Long entrustmentId);

    /**
     * 保存任务单
     *
     * @param entity
     */
    void save(TaskEntity entity);

    /**
     * 查询任务详情
     *
     * @return
     */
    TaskDetailInfoVo getTaskDetailInfo(Long taskId);

    /**
     * 修改任务信息
     */
    int updateTestTask(TaskTestEntity taskTestEntity);

    /**
     * 根据用户id 查询 团队 名
     */
    TaskTestTeamEntity selectTeamCode(Long userid);

    /**
     * 根据团队id 返回 用户集合
     *
     * @param id
     * @return
     */
    List<LabelValueTeamVo> selectTeamList(Integer id);

    /**
     * 查询任务列表
     *
     * @param paramVo
     * @return
     */
    List<TaskListVo> getTaskList(TaskListParamVo paramVo);

    /**
     * 查询领样列表
     *
     * @param paramVo
     * @return
     */
    List<ReceiveSampleListVo> getSampleList(TaskListParamVo paramVo);

    /**
     * 根据id 判断任务 state 状态
     * @param id
     * @return
     */
    Integer getJudgmentTaskList(Long id);
}
