package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DeclarationParamEntity {
    private Long planId;

    private Long productId;

    private Long checkItemId;

    private String checkItemName;

    private String attribute;

    private String createUser;

    private Date createTime;

    private Integer standardId;

    private String standardName;

    private Integer methodId;

    private String methodName;

    private Integer instrumentId;

    private String instrumentName;

    private String limitRange;

    private String description;
}
