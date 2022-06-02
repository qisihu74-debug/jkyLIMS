package com.lims.manage.erp.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ReportDetailListParamVo {
    /**
     * 委托单编号
     */
    private String entrustmentNo;
    /**
     * 任务单号
     */
    private String taskCode;
    /**
     * 报告编号
     */
    private String reportCode;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 报告合成人员
     */
    private String applicant;
    /**
     * 报告合成日期
     */
    private String reportCompleteTime;

    private Integer pageNum;
    private Integer pageSize;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endingDate;
}
