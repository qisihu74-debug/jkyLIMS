package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc 分工对象
 * @date 2024-07-09 11:12
 * @Copyright © 河南交科院
 */
@Data
@TableName("qs_audit_divide")
public class DivideEntity {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 分工id
     */
    private int divideId;
    /**
     * 活动id
     */
    private int activeId;
    /**
     *受审部门id
     */
    private String deptId;
    /**
     *受审部门名称
     */
    private String deptName;
    /**
     *审核员id
     */
    private String auditorId;
    /**
     *审核员姓名
     */
    private String auditorName;

}
