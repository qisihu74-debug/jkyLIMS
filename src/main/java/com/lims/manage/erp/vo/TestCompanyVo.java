package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/11/29 10:55
 * 单位信息
 */
@Data
public class TestCompanyVo {

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
    private Integer type;
    /**
     * 客户id
     */
    private String adminId;

    public TestCompanyVo(String companyName, Integer type, String adminId) {
        this.companyName = companyName;
        this.type = type;
        this.adminId = adminId;
    }

    public TestCompanyVo() {
    }
}
