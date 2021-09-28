package com.stu.manage.demo.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/9/23 9:48
 * 委托单基本信息1
 */
@Data
public class JtEntrustInfo {

    private Integer entrustId;

    private String entrustNumber;

    private Integer entrustType;

    private Integer entrustCompanyId;

    private String witnessCompanyName;

    private String projectId;

    private String projectPart;

    private String sampleReceiveWay;

    private String checkPurpose;

    private String productStandardSource;

    private Integer reportNum;

    private String reportGetWay;

    private String reportGetCompany;

    private String contactAddress;

    private String contactPeople;

    private String contactTel;

    private String entrustPeople;

    private String entrustPeoplePhone;

    private String witnessPeople;

    private String entrustNote;

    private Integer entrustFlowType;

    private Integer entrustCreateUserId;

    private String entrustData;

    private Integer entrustStatus;

    private String crm;

    /**
     * 数据传输需要与实体类无关
     */
    private String comName;

    private String entrustTypeName;

    private String entrustCreateUserName;

    private String financeUserName;

    private String auditTime;

    private String acceptUserId;

    private String acceptTime;

    private Integer payMode;

    private Integer financeUserId;

    private String acceptUserName;

}
