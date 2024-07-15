package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动开始 日期区间
 */
@Data
@TableName("qs_audit_schedule")
public class QsAuditScheduleEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer scheduleId;
    private Integer activeId;
    private Date startTime;
    private Date endTime;
    private String content;
}