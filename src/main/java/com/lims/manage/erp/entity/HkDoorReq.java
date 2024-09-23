package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-09-14 15:34
 * @Copyright © 河南交科院
 */
@Data
public class HkDoorReq {
    private List<PersonDoorReq> personDatas;
    private List<ResourceInfo> resourceInfos;
    private String startTime;
    private String endTime;
    /**
     * 实验室id集合
     */
    private List<Integer> laboratoryIds;
}
