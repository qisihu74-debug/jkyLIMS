package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: DLC
 * @Date: 2022/7/14 10:03
 * 流转单实体类 test_entrusted_task_rel
 */
@Data
@TableName("test_entrusted_task_rel")
public class TestEntrustedTaskRelEntity {

    /**
     * 流转单id
     */
    private Integer id;

    /**
     * 部门id
     */
    private Integer deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门id&部门名称
     */
    private String department;

    /**
     * 任务流转日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date taskFlowDate;

    /**
     * 报告类型（0,最终报告，1中间报告）
     */
    private Integer type;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String addressName;

    /**
     * 任务单id
     */
    private Long taskId;

    /**
     * 任务单编号
     */
    private String taskCode;

    /**
     * 委托单id
     */
    private Long entrustId;

    /**
     * 创建日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date createDate;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date updateDate;
    /**
     * 中间报告任务流转状态（0，未完成；1，已完成）
     */
    private Integer state;
    /**
     * 中间报告数据主键id
     */
    private Long recordId;

}
