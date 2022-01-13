package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("test_report_record")
public class ReportRecordEntity {
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

    private String applyReason;

    private String issuReason;

    private String verifyer;

    private String issuer;

    private String applicant;

    private String sealer;

    private Date sealTime;

    private String reportManager;

    private Date reportTime;

    private String addressee;

    private String waybill;

    private Date operateTime;

}