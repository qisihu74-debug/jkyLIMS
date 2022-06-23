package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class TaskListParamVo {
    /**
     * 报告单 主键
     */
    private Long taskId;
    private String taskCode;
    private String sampleName;
    private Integer state;
    private Integer teamId;
    private String receiveTime;

    private String beginDate;
    private String endDate;
    /**
     * 科室id集合
     */
    private List<Long> deptIds;
    private Integer pageNum;
    private Integer pageSize;

    /**
     * 检测人
     */
    private String inspector;
    /**
     * 复核人
     */
    private String reviewer;
}
