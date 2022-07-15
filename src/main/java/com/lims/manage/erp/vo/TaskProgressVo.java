package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.TestEntrustedTaskRelEntity;
import lombok.Data;

import java.util.List;

@Data
public class TaskProgressVo {
    /**
     * 任务单id
     */
    private Long taskId;
    /**
     * 任务编号
     */
    private String taskCode;
    /**
     * 任务状态
     * 0：任务发布
     * 1：任务领取
     * 3：试验开始
     * 4：实验完成
     * 6：复核完成
     */
    private Integer state;
    /**
     * 发布时间
     */
    private String orderTime;
    /**
     * 领取时间
     */
    private String receiveTime;
    /**
     * 试验开始时间
     */
    private String startDetectionTime;
    /**
     * 试验完成时间
     */
    private String endDetectionTime;
    /**
     * 复核时间
     */
    private String reviewTime;
    /**
     * 状态信息
     */
    List<TaskProgressStateVo> stateVoList;
    /**
     * 任务单流转集合
     */
    List<TestEntrustedTaskRelEntity> taskOrderFlowList;
}
