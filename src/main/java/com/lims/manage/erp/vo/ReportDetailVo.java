package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportDetailVo extends ReportListVo {
    private Integer reportCount;
    private String reportType;
    private List<ReportSampleDetailVo> samples;
}
