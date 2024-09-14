package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * test_task_order_working_hours_score 授权签字人与 报告检测人和审核人的评分机制表
 */
@Data
@TableName("test_task_order_working_hours_score")
public class TestTaskOrderWorkingHoursScoreEntity implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Long reportId;

    private String reportCode;

    private Long taskId;

    private String taskCode;

    private Long entrustId;

    private Long userId;

    private String userName;

    private String roleInformation;

    private Integer roleType;

    private String score;

    private Date createTime;

    private Date updateTime;
}