package com.stu.manage.demo.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/9/24 15:02
 * 样品基本信息1
 */
@Data
public class JtSampleObject {
    private Integer sampleObjectId;

    private Integer productId;

    private String modelId;

    private Integer entrustCompanyId;

    private String productNumber;

    private String productCompanyName;

    private String productDate;

    private String sendPeople;

    private Date receiveTime;

    private Integer receiveUserId;

    private String objectCount;

    private String storagePlace;

    private String receiveWay;

    private String contactTel;

    private String sampleObjectNote;

    private String sampleBatch;


    /**
     * 数据传输需要，与类字段无关
     */
    private String sampleNumber;

    private Integer sampleType;

    private List<JtEntrustCheckItem> checkList;

    private Integer sampleId;

    private String sampleStatus;
    // 样品信息下 产品标准
    private String productStandardSource;
    // 产品名称
    private String productname;

    @Override
    public String toString() {
        return "JtSampleObject{" +
                "sampleObjectId=" + sampleObjectId +
                ", productId=" + productId +
                ", modelId='" + modelId + '\'' +
                ", entrustCompanyId=" + entrustCompanyId +
                ", productNumber='" + productNumber + '\'' +
                ", productCompanyName='" + productCompanyName + '\'' +
                ", productDate='" + productDate + '\'' +
                ", sendPeople='" + sendPeople + '\'' +
                ", receiveTime=" + receiveTime +
                ", receiveUserId=" + receiveUserId +
                ", objectCount='" + objectCount + '\'' +
                ", storagePlace='" + storagePlace + '\'' +
                ", receiveWay='" + receiveWay + '\'' +
                ", contactTel='" + contactTel + '\'' +
                ", sampleObjectNote='" + sampleObjectNote + '\'' +
                ", sampleBatch='" + sampleBatch + '\'' +
                ", sampleNumber='" + sampleNumber + '\'' +
                ", sampleType=" + sampleType +
                ", checkList=" + checkList +
                ", sampleId=" + sampleId +
                ", sampleStatus='" + sampleStatus + '\'' +
                ", productStandardSource='" + productStandardSource + '\'' +
                ", productname='" + productname + '\'' +
                '}';
    }
}
