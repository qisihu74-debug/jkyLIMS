package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2021/11/19 10:43
 * @Copyright © 河南交科院
 */
@Data
@TableName("sys_dept")
public class DingDeptEntity {
    /**
     * 部门id
     */
    private String id;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 部门父级id
     */
    private int parentId;
}
