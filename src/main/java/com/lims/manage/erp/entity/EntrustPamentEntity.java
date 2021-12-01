package com.lims.manage.erp.entity;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2021/12/1 16:38
 * @Copyright © 河南交科院
 */
@Data
public class EntrustPamentEntity {
    /**
     * 委托单id
     */
    private Long entrustmentId;
    /**
     * 本次付费金额
     */
    private String price;
    /**
     * 缴费时间
     */
    private Timestamp paymentDate;
    /**
     * 操作人
     */
    private String operator;


}
