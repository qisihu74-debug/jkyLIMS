package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DeclarationProductEntity {
    private Long planId;

    private String productType;

    private String productIndex;

    private Long productId;

    private String productName;

    private String attribute;

    private String createUser;

    private Date createTime;
}
