package com.stu.manage.demo.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: DLC
 * @Date: 2021/9/27 10:25
 * 补充样品关联关系1
 */
@Data
public class JtReportInfo {
    private Integer reportId;

    private Integer fileId;

    private String reportNumber;

    private Integer flowStatus;

    private Integer sampleId;

    private Date reportDate;

    private Integer reportModality;

    private Date reportFinishDate;

    private Integer reportForm;

    private Date  submitTime;

    private String reportType;

    private Integer checkId;

    /**
     * 和表结构无关
     */
    private String sampleName;
    private Integer outCount;
    private Integer stampType;
    private String productionValue;
    private String entrustNumber;
}
