package com.stu.manage.demo.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: DLC
 * @Date: 2021/9/24 9:20
 * 委托项 价格补充信息
 */
@Data
public class JtEntrustCheckInfo {

    private Integer entrustId;

    private String sampleStatusDesc;

    private Integer sampleStay;

    private String cost;

    private Integer payMode;

    private String pay;

    private Integer acceptUserId;

    private Date acceptTime;

    private Date endPlanDate;

    private Integer source;

    private String standardCost;

    /**
     * 传输需要与表结构无关
     */

    private String payModeName;

    private String userName;

    private String entrustSourceName;
}
