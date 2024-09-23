package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("test_instrument")
public class DeviceEntity {
    private Integer id;

    private String code;

    private String name;

    private String filesState;

    private String model;

    private String manufacturer;

    private String serialNumber;

    private String price;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date appraisalDate;
    @TableField(value = "`range`")
    private String range;
    @TableField(value = "`level`")
    private String level;

    private String used;

    private Integer laboratoryId;

    private String oldCode;

    private String deviceAdmin;

    private String useDept;

    private String calibrationParam;

    private String storePlace;

    private String calibrationPeriod;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireDate;

    private String calibrationNumber;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date purchaseDate;

    private String isCalibration;

    private String affirmWay;

    private String calibrationCorporation;

    private String pricePerHour;

    private String picture;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date incorporatedDate;

    private Integer typeId;

    private String isPort;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
    @TableField(exist = false)
    private Integer pageSize;
    @TableField(exist = false)
    private Integer pageNum;

}