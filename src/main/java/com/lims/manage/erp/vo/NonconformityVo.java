package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class NonconformityVo {
    private String auditName;
    private String auditTime;
    private String deptName;
    private String auditorName;
    private String deptLeader;
    private String state;
    private String receivedDate;
    private String requiredDate;
    private String actualDate;
    private String verifyDate;
    private String measures;
    private String verification;
}