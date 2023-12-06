package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;

@Data
public class StandardMethodEntity {
    private Integer standardId;

    private String chapterNum;

    private String chapterName;

    private Date createTime;
}