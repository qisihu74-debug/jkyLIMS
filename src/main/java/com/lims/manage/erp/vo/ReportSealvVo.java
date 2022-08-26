package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2022/8/24 15:54
 * 报告电子印章信息
 */
@Data
public class ReportSealvVo {

    private Long id;

    /**
     *合同id
     */
    private Long contractId;

    /**
     * 报告编号
     */
    private String reportCode;

    /**
     *电子报告盖章完成url
     */
    private String sealReportUrl;

    /**
     *  委托单ID用于最终报告查询
     */
    private Long entrustmentId;

    /**
     * 委托单ID用于中间报告查询
     */
    private Long entrustId;
    /**
     * type 1最终报告 0 是中间报告
     */
    private Integer type;
    /**
     * 报告提交申请人
     */
    private String applicant;
}
