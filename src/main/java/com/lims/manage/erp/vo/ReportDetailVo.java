package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportDetailVo extends ReportListVo {
    private Integer reportCount;
    private Integer taskFlowId;
    private String reportType;
    private String entrustTestType;
//    private Long productId;
    private List<ReportSampleDetailVo> samples;
}
