package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class ReportListVo {
    private Long id;
    private String taskCode;
    private String sampleName;
    private String countPrice;
    private String requestDate;
    /**
     * 报告生成时间
     */
    private String operateTime;
}
