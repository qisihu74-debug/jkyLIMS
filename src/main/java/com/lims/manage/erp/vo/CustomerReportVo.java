package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class CustomerReportVo {
    private Long reportId;
    private String reportCode;
    private String sampleName;
    private String reportType;
    private String reportState;
    private String reportTime;
    private String sealTime;
    private String issuerTime;
    private Long entrustId;
    private String entrustmentNo;
    private String entrustCompany;
    private String projectName;
    @JsonIgnore
    private String downloadUrl;
}
