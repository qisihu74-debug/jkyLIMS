package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2022/8/12 11:47
 * 单位下联系人员信息 VO层
 */
@Data
public class TestCustomerVo {
    /**
     * 主键
     */
    private Integer Id;
    /**
     * 单位id
     */
    private Integer companyId;
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




}
