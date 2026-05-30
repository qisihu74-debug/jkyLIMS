package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ReportRecordMidEntity {
    private Long id;

    private Long entrustmentId;

    private String reportCode;

    private String sampleName;

    private String price;

    private Date requiredCompletionTime;

    private String taskCode;

    private String state;

    private Integer number;

    private String reportType;

    private Date verifyerTime;

    private Date issuerTime;

    private String sealType;

    private String sealUrl;

    private String applyReason;

    private String issuReason;

    private String verifyer;

    private Long verifyerId;

    private String issuer;

    private Long issuerId;

    private String applicant;

    private String sealer;

    private Date sealTime;

    private String reportManager;

    private Date reportTime;

    private String addressee;

    private String waybill;

    private Date operateTime;

    private String email;

    private String reportPhone;

    private String reportMailingAddress;

    private Date reportCompleteTime;

    private String templateName;

    private String qysDocmentId;

    private String qysState;

    private String contractId;

    private String signUrl;

    private Long taskId;

    private String type;

    private String category;

    private Date combineTime;

    private Long entrustId;

    private String inspector;

    private String reportUrl;
    /**
     * 操作类型0线上编辑的报告，1线下编辑的报告
     */
    private Integer operateType;

    public ReportRecordMidEntity() {
    }

    public ReportRecordMidEntity(ReportRecordEntity recordEntity) {
        this.id = recordEntity.getId();
        this.entrustmentId = recordEntity.getEntrustmentId();
        this.reportCode = recordEntity.getReportCode();
        this.sampleName = recordEntity.getSampleName();
        this.price = recordEntity.getPrice();
        this.requiredCompletionTime = recordEntity.getRequiredCompletionTime();
        this.taskCode = recordEntity.getTaskCode();
        this.state = recordEntity.getState();
        this.number = recordEntity.getNumber();
        this.reportType = recordEntity.getReportType();
        this.verifyerTime = recordEntity.getVerifyerTime();
        this.issuerTime = recordEntity.getIssuerTime();
        this.sealType = recordEntity.getSealType();
        this.sealUrl = recordEntity.getSealUrl();
        this.applyReason = recordEntity.getApplyReason();
        this.issuReason = recordEntity.getIssuReason();
        this.verifyer = recordEntity.getVerifyer();
        this.verifyerId = recordEntity.getVerifyerId();
        this.issuer = recordEntity.getIssuer();
        this.issuerId = recordEntity.getIssuerId();
        this.applicant = recordEntity.getApplicant();
        this.sealer = recordEntity.getSealer();
        this.sealTime = recordEntity.getSealTime();
        this.reportManager = recordEntity.getReportManager();
        this.reportTime = recordEntity.getReportTime();
        this.addressee = recordEntity.getAddressee();
        this.waybill = recordEntity.getWaybill();
        this.operateTime = recordEntity.getOperateTime();
        this.email = recordEntity.getEmail();
        this.reportPhone = recordEntity.getReportPhone();
        this.reportMailingAddress = recordEntity.getReportMailingAddress();
        this.reportCompleteTime = recordEntity.getReportCompleteTime();
        this.templateName = recordEntity.getTemplateName();
        this.qysDocmentId = recordEntity.getQysDocmentId();
        this.qysState = recordEntity.getQysState();
        this.contractId = recordEntity.getContractId();
        this.signUrl = recordEntity.getSignUrl();
        this.taskId = recordEntity.getTaskId();
        this.type = recordEntity.getType();
        this.category = recordEntity.getCategory();
        this.combineTime = recordEntity.getCombineTime();
        this.entrustId = recordEntity.getEntrustId();
        this.inspector = recordEntity.getInspector();
        this.reportUrl = recordEntity.getReportUrl();
        this.operateType = recordEntity.getOperateType();
    }
}