package com.stu.manage.demo.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/9/24 9:39
 * 委托基本信息展示方式
 */
@Data
public class jtEntrustType {

    /**
     * 委托方式
     */
    private Integer entrustType;
    private String entrustTypeName;

    /**
     * 检验目的
     */
    //检验目的id
    private Integer checkPurposeId;
    // 检验项
    private String checkPurposeItem;

    /**
     * 取样方式
     */
    //取样方式id
    private Integer receiveWay;
    //取样名称
    private String receiveWayName;
    /**
     * 取报告方式
     */
    // 取报告id
    private Integer reportGetWayId;
    // 取报告项
    private String reportGetWay;



}
