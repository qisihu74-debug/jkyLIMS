package com.stu.manage.demo.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc 菜单实体bean
 * @date 2021/9/22 10:14
 * @Copyright © 河南交科院
 */
@Data
public class FunctionEntity {
    /**
     * 菜单id
     */
    private Long id;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 菜单上级id
     */
    private Long parentId;
    /**
     * 组件名称
     */
    private String objName;
    /**
     * 菜单图标名称
     */
    private String iconName;
    /**
     * 菜单排序号
     */
    private int sort;
}
