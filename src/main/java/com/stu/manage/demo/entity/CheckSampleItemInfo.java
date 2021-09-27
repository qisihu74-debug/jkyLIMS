package com.stu.manage.demo.entity;

import java.util.Date;

/**
 * @Author: DLC
 * @Date: 2021/9/26 9:28
 */
public class CheckSampleItemInfo {
    private Integer csiId;

    private Integer entrustId;

    private Integer checkId;

    private Integer sampleId;

    private Integer checkItemId;

    private Date checkTime;

    private String abnormal;

    private Integer checkUserId;

    private Integer itemCheckTaskId;

    private Integer entrustCheckItemId;


    /**
     * 传输需要，和实体类无关
     */
    private String checkItemName;

    private JtEntrustCheckItem item;
}
