package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/11/29 10:55
 * 委托单位信息
 */
@Data
public class TestCompanyJsonEntity {

    private Integer companyId;
    /**
     * 委托单位名称
     */
    private String companyName;
    /**
     * 联系人
     */
    private String contacts;
    /**
     * 联系方式
     */
    private String contactWay;
    /**
     * 地址
     */
    private String address;
    /**
     * 类型
     */
    private String type;
}
