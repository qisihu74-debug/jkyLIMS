package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DeviceEntity {
    private Integer id;

    private Integer oldId;

    private String code;

    private String name;

    private String filesState;

    private String model;

    private String manufacturer;

    private String serialNumber;

    private String price;

    private Date appraisalDate;

    private String range;

    private String level;

    private String used;

    private Integer laboratoryId;

    private String oldCode;

    private String deviceAdmin;

    private String useDept;

    private String calibrationParam;

    private String storePlace;

    private String calibrationPeriod;

    private Date expireDate;

    private String calibrationNumber;

    private Date purchaseDate;

    private String isCalibration;

    private String affirmWay;

    private String calibrationCorporation;

    private String pricePerHour;

    private String picture;

    private Date incorporatedDate;

    private Integer typeId;

    private String isPort;

    private Date validityDate;

    private String content;

    private String checkPrice;

    private String specs;

    private String traceabilityMode;

    private String contractUrl;

    private String invoiceUrl;

    private String remark;

    private String status;

    private Integer delFlag;

    private Date createTime;

    private Date updateTime;
}