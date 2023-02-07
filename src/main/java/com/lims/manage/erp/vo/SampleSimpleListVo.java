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
    private String sampleType;
    /**
     * 样品别名
     */
    private String aliasName;
    /**
     * 样品状态：待检0；领样1；在检2；已检3；
     */
    private String state;
    /**
     * 是否留样1.保留2.处置
     */
    private String isSave;
    /**
     * 样品保留天数
     */
    private Long saveTime;
}
