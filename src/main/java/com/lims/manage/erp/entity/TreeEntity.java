package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022-07-18 16:33
 * @Copyright © 河南交科院
 */
@Data
public class TreeEntity {
    private String id;

    private String pid;

    private String name;

    private List<TreeEntity> children;
}
