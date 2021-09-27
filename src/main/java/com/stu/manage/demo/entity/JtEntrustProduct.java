package com.stu.manage.demo.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/9/26 14:33
 * 保存委托和产品关系
 */
@Data
public class JtEntrustProduct {
    private Integer entrustProductId;

    private Integer entrustId;

    private Integer productId;
}
