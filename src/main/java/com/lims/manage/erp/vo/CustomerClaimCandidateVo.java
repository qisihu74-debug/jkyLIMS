package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class CustomerClaimCandidateVo {
    private Integer candidateCompanyId;
    private String companyName;
    private String companyAddress;
    private Integer candidateCustomerId;
    private String contactName;
    private String contactPhone;
    private String matchBasis;
}
