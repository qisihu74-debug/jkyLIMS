package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lims.manage.erp.vo.ReportPreserveVo;
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

    public ReportRecordEntity() {
    }

    public ReportRecordEntity(ReportPreserveVo vo) {
        this.id = vo.getId() == null ? null : vo.getId();
        this.entrustmentId = vo.getId() == null ? null : vo.getId();
        this.reportCode = vo.getReportCode() == null ? null : vo.getReportCode();
        this.sampleName = vo.getSampleName() == null ? null : vo.getSampleName();
        this.price = vo.getPrice() == null ? null : vo.getPrice();
        this.requiredCompletionTime = vo.getRequiredCompletionTime() == null ? null : vo.getRequiredCompletionTime();
        this.taskCode = vo.getTaskCode() == null ? null : vo.getTaskCode();
        this.state = vo.getState() == null ? null : vo.getState();
        this.number = vo.getNumber() == null ? null : vo.getNumber();
        this.reportType = vo.getReportType() == null ? null : vo.getReportType();
        this.verifyerTime = vo.getVerifyerTime() == null ? null : vo.getVerifyerTime();
        this.issuerTime = vo.getIssuerTime() == null ? null : vo.getIssuerTime();
        this.sealType = vo.getSealType() == null ? null : vo.getSealType();
        this.applyReason = vo.getApplyReason() == null ? null : vo.getApplyReason();
        this.issuReason = vo.getIssuReason() == null ? null : vo.getIssuReason();
        this.verifyer = vo.getVerifyer() == null ? null : vo.getVerifyer();
        this.issuer = vo.getIssuer() == null ? null : vo.getIssuer();
        this.applicant = vo.getApplicant() == null ? null : vo.getApplicant();
        this.sealer = vo.getSealer() == null ? null : vo.getSealer();
        this.sealTime = vo.getSealTime() == null ? null : vo.getSealTime();
        this.reportManager = vo.getReportManager() == null ? null : vo.getReportManager();
        this.reportTime = vo.getReportTime() == null ? null : vo.getReportTime();
        this.addressee = vo.getAddressee() == null ? null : vo.getAddressee();
        this.waybill = vo.getWaybill() == null ? null : vo.getWaybill();
        this.operateTime = vo.getOperateTime() == null ? null : vo.getOperateTime();
    }
}