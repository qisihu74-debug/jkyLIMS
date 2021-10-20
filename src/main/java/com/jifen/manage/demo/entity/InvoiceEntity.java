package com.jifen.manage.demo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author gjl
 * @version V1.0
 * @Package com.jifen.manage.demo.entity
 * @desc
 * @date 2021/10/15 16:05
 * @Copyright © 河南交科院
 */
@Data
public class InvoiceEntity {
    /**
     * 发票代码
     */
    private String invoiceCode;
    /**
     * 发票号码
     */
    private String InvoiceNum;
    /**
     * 开票时间
     */
    private String invoiceDate;
    /**
     * 发票种类
     */
    private String InvoiceTypeOrg;
    /**
     * 校验码
     */
    private String CheckCode;
    /**
     * 购方名称
     */
    private String purchaserName;
    /**
     * 购方纳税人识别号
     */
    private String purchaserRegisterNum;
    /**
     * 价税合计
     */
    private String amountInFiguers;
    /**
     * 售方名称
     */
    private String sellerName;
    /**
     * 售方纳税人识别号
     */
    private String sellerRegisterNum;
    /**
     * 发票是否为真，true真，false假
     */
    private Boolean flag;
    /**
     * 注释
     */
    private String message;
}
