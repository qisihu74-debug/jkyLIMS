package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/11/22 9:43
 * 树结构
 */
@Data
public class TreeFunction {
    private Long functionId;
    private String treeName;
    private Long functionPid;
    private Integer sort;
    private boolean catesFlag;
    private List Children = new ArrayList();
    private Long userId;
    private String userName;
    /**
     * 菜单类型
     */
    private String dataType;
    /**
     * 菜单名称
     */
    private String menuValue;
    /**
     * 菜单对应权限
     */
    private List<SysMenuEntity> menuEntityList = new ArrayList<>();
}
