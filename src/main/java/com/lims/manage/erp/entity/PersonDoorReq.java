package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-09-14 15:27
 * @Copyright © 河南交科院
 */
@Data
public class PersonDoorReq {
    /**
     * 人员标识集合
     */
    private List<String> indexCodes;
    /**
     * 人员数据类型 person
     */
    private String personDataType;
}
