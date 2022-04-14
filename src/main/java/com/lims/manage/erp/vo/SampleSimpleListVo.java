package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class SampleSimpleListVo {
    private Long id;
    private Integer productId;
    private String sampleName;
    private String sampleCode;
    private String manufacturer;
    private String specs;
    private String inspector;
    private String generation;
    private String receivedDate;
    private String batchNumber;
}
