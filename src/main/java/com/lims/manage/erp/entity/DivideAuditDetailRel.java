package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-07-09 11:31
 * @Copyright © 河南交科院
 */
@Data
@TableName("qs_audit_divide_rel")
public class DivideAuditDetailRel {
    /**
     * 分工id
     */
    @TableId
    private int divideId;
    /**
     *不符合项
     */
    private String nonConformance;
    /**
     *不符合程序
     */
    private String nonConformanceProgram;
    /**
     *不符合标准
     */
    private String substandard;
    /**
     *不符合程度
     */
    private String nonComplianceDegree;
    /**
     *检查结果
     */
    private String checkResult;
    /**
     *审核日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date checkDate;
    /**
     *'检查待开始','检查中','检查完成','措施验证','已完成'
     */
    private String state;
    @TableField(exist = false)
    List<DivideAuditDetail> list;
    @TableField(exist = false)
    private int activeId;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private String userName;
}
