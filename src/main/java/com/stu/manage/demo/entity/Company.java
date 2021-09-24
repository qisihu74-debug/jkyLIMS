package com.stu.manage.demo.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/9/23 17:55
 * 补充委托公司信息
 */
@Data
public class Company {
    private Integer comId;
    /**
     * 委托公司名
     */
    private String comName;
    private String comAddress;
    private String comContactPerson;
    private String comContactPhone;

    private Integer count;
}
