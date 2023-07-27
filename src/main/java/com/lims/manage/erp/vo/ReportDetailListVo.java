package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportDetailListVo {
    private Long reportId;
    /**
     * 委托单ID
     */
    private Long entrustId;
    /**
     * 委托单编号
     */
    private String entrustmentNo;
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
    /**
     * 报告领取人
     */
    private String addressee;
    /**
     * 报告领取时间
     */
    private String reportTime;
    /**
     * 邮寄编号
     */
    private String postCode;
    /**
     * 任务单号集合
     */
    private List<TaskCodeVo> taskCodes;

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
