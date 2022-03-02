package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class TaskListVo {
    private Long taskId;//任务主键
    private String taskCode;//任务编号
    private String inspector;
    private Integer productId;
    private String sampleName;
    private String cost;//本单费用
    private String requiredCompletionTime;//完成日期
    private Integer state;//完成日期
    private String recorder;
    private String reviewer;
    private String reportProducer;
    /**
     * 是否出具报告
     */
    private String issueReport;
}
