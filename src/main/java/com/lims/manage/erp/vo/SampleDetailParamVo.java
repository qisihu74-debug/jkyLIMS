package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;
@Data
public class SampleDetailParamVo {
    private Integer productId;
    private String manufacturer;
    private String batchNumber;
    private String receivedDate;
    private List<String> states;
}
