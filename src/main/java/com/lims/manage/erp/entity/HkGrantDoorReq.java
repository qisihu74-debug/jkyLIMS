package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-09-19 11:33
 * @Copyright © 河南交科院
 */
@Data
public class HkGrantDoorReq {
    /**
     *1：卡片 4：人脸
     */
    private int taskType;
    private List<ResourceInfo> resourceInfos;

}
