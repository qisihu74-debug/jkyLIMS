package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class TaskCodeVo {
    private Long entrustmentId;
    /**
     * 任务编号
     */
    private String taskCode;
    /**
     * 分配的科室
     */
    private String teamName;
    /**
     * 任务单状态
     */
    private String taskState;
}
