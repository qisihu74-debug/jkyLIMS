package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class TaskListParamVo {
    private String taskCode;
    private String sampleName;
    private Integer state;
    private Integer teamId;
    private String receiveTime;

    private String beginDate;
    private String endDate;
}
