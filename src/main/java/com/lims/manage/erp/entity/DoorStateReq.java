package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-09-24 11:36
 * @Copyright © 河南交科院
 */
@Data
public class DoorStateReq {
    private List<String> doorIndexCodes;
}
