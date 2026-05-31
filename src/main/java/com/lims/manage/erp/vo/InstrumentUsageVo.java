package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class InstrumentUsageVo {
    private String name;
    private String code;
    private String lab;
    private Integer useCount;
    private Double useHours;
}