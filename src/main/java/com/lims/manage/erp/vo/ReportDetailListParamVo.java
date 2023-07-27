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

    private String aliasName;
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
    /**
     * 报告类型 （0最终报告,1中间报告）
     */
    private Integer reportTypeStatus;

    /**
     * 物理章、电子章
     */
    private String category;
    /**
     * 委托编号——String
     */
    private String entrustmentNostr;
    /**
     * 委托编号类别： null 常规原材试验、MN模拟试验、BD比对试验
     */
    private String entrustCategoryType;
    /**
     * 邮寄人
     */
    private String reportManager;

    /**
     * 邮寄单号
     */
    private String waybill;

    /**
     * 委托单位
     */
    private String entrustCompany;
}
