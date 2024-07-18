package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

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
    private Long id;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 部门父级id
     */
    private Long parentId;
    /**
     * 父部门名称
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 部门编号
     */
    private String code;

    /**
     * 部门职责
     */
    private String duty;
    /**
     * 备注/描述
     */
    private String remark;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;

    /**
     * 修改时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    /**
     * 负责人ID
     */
    private String userId;
    /**
     * 负责人名字
     */
    private String userName;
}
