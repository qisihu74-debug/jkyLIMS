package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class EntrustSampleInfoVo {
    private Integer sampleId;
    private String sampleType;
    private String aliasName;
    private String specs;
    private String outwardDescribe;
    private Long entrustId;
}
