package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 会议纪要:具体内容
 */
@Data
@TableName("qs_active_schedule_rel")
public class QsAuditScheduleRelEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer activeId;
    /**
     * 会议名称
     */
    private String meetingName;
    /**
     * 会议地址
     */
    private String meetingAdress;
    /**
     * 主持人Id
     */
    private String hostUserId;
    /**
     * 主持人名称
     */
    private String hostName;
    /**
     * 记录人Id
     */
    private String recorderUserId;
    /**
     * 记录人
     */
    private String recorderName;
    /**
     * 出席人
     */
    private String Attendance;
    /**
     * 内容
     */
    private String content;
    /**
     * 会议结果落实
     */
    private String meetingResult;
    /**
     * 附件url
     */
    private String url;

    /**
     * 会议类型
     */
    private String meetingType;
    /**
     * 会议周期
     */
    @TableField(exist = false)
    private String meetingCycle;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date endTime;
    /**
     * 组员对象集合
     */
    @TableField(exist = false)
    private List<AuditTeamNumber> auditTeamList = new ArrayList<>();

    /**
     * 类型: type =1 首次会议 type = 2 末次会议
     */
    @TableField(exist = false)
    private Integer type;
}