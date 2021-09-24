package com.stu.manage.demo.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/9/24 15:02
 * 样品基本信息
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

    private List<JtEntrustCheckItem> checkList;


}
