package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("test_report_record")
public class ReportRecordEntity {
    private Long id;
    /**
     * 委托单id
     */
    private Long entrustmentId;
    /**
     * 报告编号
     */
    private String reportCode;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 本单价钱
     */
    private String price;
    /**
     * 要求完成时间
     */
    private Date requiredCompletionTime;
    /**
     * 任务编号
     */
    private String taskCode;
    /**
     * 状态报告状态，0报告被驳回 1指标填写已完成，2指标填写未完成，3.审批已抢单，
     * 4.签发待抢单，5.签发已抢单，6已签发，7盖章，8已邮寄
     */
    private String state;
    /**
     * 报告份数
     */
    private Integer number;
    /**
     * 取报告方式
     */
    private String reportType;
    /**
     *审批时间
     */
    private Date verifyerTime;
    /**
     *签发时间
     */
    private Date issuerTime;
    /**
     *盖章类型
     */
    private String sealType;
    /**
     *审批驳回原因
     */
    private String applyReason;
    /**
     *签发驳回原因
     */
    private String issuReason;
    /**
     *审核人姓名
     */
    private String verifyer;
    /**
     *签发人
     */
    private String issuer;
    /**
     *报告提交申请人
     */
    private String applicant;
    /**
     *盖章人
     */
    private String sealer;
    /**
     *盖章时间
     */
    private Date sealTime;
    /**
     *报告发出人
     */
    private String reportManager;
    /**
     *报告发出时间
     */
    private Date reportTime;
    /**
     *领取报告人员
     */
    private String addressee;
    /**
     *运单编号
     */
    private String waybill;
    /**
     *操作时间，报告发出后录入数据时的时间
     */
    private Date operateTime;

}