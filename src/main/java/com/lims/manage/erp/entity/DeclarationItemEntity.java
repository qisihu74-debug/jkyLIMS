package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DeclarationItemEntity {
    private Long planId;

    private Long productId;

    private Long checkItemId;

    private String checkItemName;

    private String attribute;

    private String createUser;

    private Date createTime;

    private List<DeclarationParamEntity> paramEntity;

    private Integer pageNum;
    private Integer pageSize;
}
