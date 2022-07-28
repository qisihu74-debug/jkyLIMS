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

    private Long qysDocmentId;

    private String qysState;

    private Long contractId;

    private String signUrl;

    private Long taskId;

    private String type;

    private String category;

    private Date combineTime;

    private Long entrustId;

    private String inspector;

    private String reportUrl;
}