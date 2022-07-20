package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportListVo {
    private Long id;
    private Long taskId;
    private String taskCode;
    private String reportCode;
    private String sampleName;
    private String countPrice;
    private String requestDate;
    private String reportUrl;
    private String entrustTestType;
    /**
     * 报告生成时间
     */
    private String reportCompleteTime;
    /**
     * state 状态
     */
    private Integer state;
    private Integer productId;
    /**
     * 任务流转ID
     */
    private Integer taskFlowId;
    private String entrustmentNo;

    private List<Long> deptIds;

}
