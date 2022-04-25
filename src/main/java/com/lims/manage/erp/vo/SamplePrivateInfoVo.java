package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * 单个样品自有信息
 */
@Data
public class SamplePrivateInfoVo {
    private Integer id;
    private Integer pid;
    private String sampleCode;
    private String batchNumber;
    private String picture;
    private String insertFlag;
    private String state;
    private String aliasName;
    private String sampleType;
}
