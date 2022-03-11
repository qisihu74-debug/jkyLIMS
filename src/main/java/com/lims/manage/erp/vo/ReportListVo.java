package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportListVo {
    private Long id;
    private Long taskId;
    private String taskCode;
    private String sampleName;
    private String countPrice;
    private String requestDate;
    private String reportUrl;
    /**
     * 报告生成时间
     */
    private String reportCompleteTime;
    /**
     * state 状态
     */
    private Integer state;
    private Integer productId;

    private List<Long> deptIds;

}
