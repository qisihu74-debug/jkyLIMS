package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @date 2024-07-09 11:38
 * @Copyright © 河南交科院
 */
@Data
@TableName("qs_audit_active")
public class QsActiveEntity {
    @TableId(type = IdType.AUTO)
    private int activeId;
    private String name;
    private String purpose;
    private String nature;
    private String rangeText;
    private String basis;
    private String group_leader_id;
    private String groupLeaderName;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date endTime;
    private String points;
    private String editorId;
    private String editorName;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private String editorDate;
    private String url;
    private String state;

    /**
     * 组员对象集合
     */
    @TableField(exist = false)
    private List<AuditTeamNumber> auditTeamList;
    /**
     * 分工对象集合
     */
    @TableField(exist = false)
    private List<DivideEntity> divideList;
    /**
     * 日程安排
     */
    @TableField(exist = false)
    private List<QsAuditScheduleEntity> qsAuditScheduleEntityList;

}
