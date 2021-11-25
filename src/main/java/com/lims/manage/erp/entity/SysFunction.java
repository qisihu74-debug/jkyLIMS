package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
    private int sort;
    @TableField(exist = false)
    private Boolean flag;

}
