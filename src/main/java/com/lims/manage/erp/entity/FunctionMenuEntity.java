package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2021/11/24 10:35
 * @Copyright © 河南交科院
 */
@Data
public class FunctionMenuEntity {
    /**
     * 菜单id
     */
    private Long functionId;
    /**
     * 菜单名称
     */
    private String functionName;
    /**
     * 菜单父id
     */
    private Long functionPid;
    private int sort;
    /**
     * 权限ids
     */
    private List<SysMenuEntity> menuIds;
}
