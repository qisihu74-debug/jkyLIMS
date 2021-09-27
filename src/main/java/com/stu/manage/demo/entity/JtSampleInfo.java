package com.stu.manage.demo.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/9/26 9:25
 * 样品关联关系
 */
@Data
public class JtSampleInfo {

    private Integer sampleId;

    private Integer productId;

    private Integer entrustId;

    private String sampleNumber;

    private Integer sampleType;

    private Integer entrustCompanyId;

    private String samplingBaseCount;

    private String samplingPlace;

    private String sampleStatus;

    private Integer sampleObjectId;

    private String objectRatio;

    private String sampleNote;

    private Integer lifecycle;

    private Integer fileId;



    /**
     * 数据传输需要与实体类无关
     */
    private String receiveUserName;
    private String lifecycleName;
    private Date receiveTime;
    private String sampleTypeName;
    private Integer receiveUserId;
    private String modelId;
    private String productNumber;
    private String productCompanyName;
    private String objectCount;
    private String storagePlace;
    private String comName;
    private String sampleObjectNote;
    private String sampleBatch;
    private List<JtEntrustCheckItem> checkList;
    private Integer flowStatus;
    private String productName;
    private List<CheckSampleItemInfo> itemInfoList;
}
