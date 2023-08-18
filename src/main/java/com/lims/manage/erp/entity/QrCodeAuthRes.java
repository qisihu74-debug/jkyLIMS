package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2023-08-17 17:21
 * @Copyright © 河南交科院
 */
@Data
public class QrCodeAuthRes {
    /**
     * 报告编号
     */
    private String reportCode;
    /**
     * 检测机构
     */
    private String checkOrganization;
    /**
     * 委托单位
     */
    private String entrustCompany;
    /**
     * 工程名称
     */
    private String projectName;
    /**
     * 委托人
     */
    private String entrustPeople;
    /**
     * 委托日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date acceptanceDate;
    /**
     * 样品编号
     */
    private String sampleCode;
    /**
     * 样品名称
     */
    private String sampleName;
    private String specs;

}
