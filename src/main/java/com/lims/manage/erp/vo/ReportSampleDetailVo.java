package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportSampleDetailVo {
    private Long sampleId;
    private Long productId;
    private String sampleCode;
    private String sampleName;
    private String specs;
    private String standard;
    private String outward;
    private List<ReportCheckItemDetailVo> checkItems;
}
