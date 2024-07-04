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
     * 菜单属性
     */
    private String menuType;
    /**
     * 管理页值
     */
    private String manageContent;

    /**
     * 拼接字符
     */
    private String concatenatedText;
    /**
     * 菜单对应权限
     */
    private List<SysMenuEntity> menuEntityList = new ArrayList<>();

    public TreeFunction() {
    }

    public TreeFunction(SysFunction sysFunction) {
        this.functionId = sysFunction.getFunctionId();
        this.treeName = sysFunction.getName();
        this.functionPid = sysFunction.getFunctionPid();
        this.sort = sysFunction.getSort() != null ? sysFunction.getSort() : null;
        this.dataType = sysFunction.getDataType() != null ? sysFunction.getDataType() : null;
        this.menuValue = sysFunction.getMenuValue() != null ? sysFunction.getMenuValue() : null;
        this.menuType = sysFunction.getMenuType() != null ? sysFunction.getMenuType() : null;
        this.concatenatedText = sysFunction.getManageContent() != null ? sysFunction.getManageContent() : null;
    }
}
