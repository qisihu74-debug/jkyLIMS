package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-01-25 11:19
 * @Copyright © 河南交科院
 */
@TableName("heat_exchanger")
public class Pricebeans {

    private String elecentity_en;
    private String price;
    private String standard_carrier;
    private String language;
    private String elecentity;
    private String standard_carrier_en;
    public void setElecentity_en(String elecentity_en) {
        this.elecentity_en = elecentity_en;
    }
    public String getElecentity_en() {
        return elecentity_en;
    }

    public void setPrice(String price) {
        this.price = price;
    }
    public String getPrice() {
        return price;
    }

    public void setStandard_carrier(String standard_carrier) {
        this.standard_carrier = standard_carrier;
    }
    public String getStandard_carrier() {
        return standard_carrier;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    public String getLanguage() {
        return language;
    }

    public void setElecentity(String elecentity) {
        this.elecentity = elecentity;
    }
    public String getElecentity() {
        return elecentity;
    }

    public void setStandard_carrier_en(String standard_carrier_en) {
        this.standard_carrier_en = standard_carrier_en;
    }
    public String getStandard_carrier_en() {
        return standard_carrier_en;
    }

}
