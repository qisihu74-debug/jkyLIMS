package com.lims.manage.erp.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CustomerClaimAdminVo {
    private Long id;
    private Long accountId;
    private String accountMobile;
    private String accountName;
    private Integer candidateCompanyId;
    private String companyName;
    private Integer candidateCustomerId;
    private String contactName;
    private String contactPhone;
    private String matchBasis;
    private String status;
    private String applyRemark;
    private String reviewRemark;
    private Long reviewUserId;
    private Timestamp reviewTime;
    private Timestamp createTime;
}
