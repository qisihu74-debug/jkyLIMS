package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.entity
 * @desc
 * @date 2021/11/10 11:36
 * @Copyright © 河南交科院
 */
@Data
@TableName("sys_function")
public class SysFunction implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 菜单id
     */
    @TableId(type = IdType.AUTO)
    private Long functionId;
    /**
     * 菜单父ID
     */
    private Long functionPid;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 菜单序号
     */
    private Integer sort;
    @TableField(exist = false)
    private Boolean flag;

    /**
     * 菜单类型
     */
    private String dataType;

    /**
     * 菜单标识
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

    public SysFunction() {
    }

    public SysFunction(TreeFunction treeFunction) {
        this.functionId = treeFunction.getFunctionId();
        this.functionPid = treeFunction.getFunctionPid();
        this.name = treeFunction.getTreeName();
        this.sort = treeFunction.getSort() != null ? treeFunction.getSort() : null;
        this.dataType = treeFunction.getDataType() != null ? treeFunction.getDataType() : null;
        this.menuValue = treeFunction.getMenuValue() != null ? treeFunction.getMenuValue() : null;
        this.menuType = treeFunction.getMenuType() != null ? treeFunction.getMenuType() : null;
        this.manageContent = treeFunction.getManageContent() != null ? treeFunction.getManageContent() : null;
    }
}
