package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class ReportNodeVo {
    /**
     * 报告编号
     */
    private String reportCode;
    /**
     * 状态报告状态，0报告被驳回 1指标填写已完成，2指标填写未完成，3.审批已抢单，
     * 4.签发待抢单，5.签发已抢单，6已签发，7盖章，8已邮寄
     */
    private String state;
    /**
     * 报告生成时间（state=1时）
     */
    private String reportCompleteTime;
    /**
     * 报告合并时间
     */
    private String combineTime;
    /**
     * 审批时间
     */
    private String verifyerTime;
    /**
     * 签发时间
     */
    private String issuerTime;
    /**
     * 盖章时间
     */
    private String sealTime;
    /**
     * 操作时间，报告发出后录入数据时的时间
     */
    private String operateTime;
}
