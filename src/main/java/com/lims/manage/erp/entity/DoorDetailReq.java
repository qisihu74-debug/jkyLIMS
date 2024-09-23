package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-09-23 14:58
 * @Copyright © 河南交科院
 */
@Data
public class DoorDetailReq {
//    {
//        "pageNo": 1,
//            "pageSize": 10,
//            "doorIndexCodes": [
//        "1f276203e5234bdca08f7d99e1097bba"
//    ],
//        "startTime": "2018-05-21T12:00:00+08:00",
//            "endTime": "2018-05-21T12:00:00+08:00",
//            "personName": "xx"
//
//    }
    private Integer pageNo;
    private Integer pageSize;
    private List<String> doorIndexCodes;
    private String startTime;
    private String endTime;
    private String personName;
}
