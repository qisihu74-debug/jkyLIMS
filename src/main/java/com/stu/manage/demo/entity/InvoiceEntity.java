package com.stu.manage.demo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc
 * @date 2021/10/15 16:05
 * @Copyright © 河南交科院
 */
@Data
public class InvoiceEntity {
    private String invoiceCode; //发票代码
    private String invoiceDate; //开票时间
    private String purchaserName; //购方名称
    private String purchaserRegisterNum; //购方纳税人识别号
    private BigDecimal amountInFiguers; //价税合计
    private String sellerName; //售方名称
    private String sellerRegisterNum; //售方纳税人识别号
    @Override
    public String toString() {
        return "Invoice [invoiceCode=" + invoiceCode + ", invoiceDate=" + invoiceDate + ", purchaserName="
                + purchaserName + ", purchaserRegisterNum=" + purchaserRegisterNum + ", amountInFiguers="
                + amountInFiguers + ", sellerName=" + sellerName + ", sellerRegisterNum=" + sellerRegisterNum + "]";
    }
}
