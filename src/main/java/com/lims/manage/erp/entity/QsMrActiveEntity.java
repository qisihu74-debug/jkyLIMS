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
 * @date 2024-07-26 11:03
 * @Copyright © 河南交科院
 */
@Data
@TableName("qs_mr_active")
public class QsMrActiveEntity {
    /**
     * 内审活动id
     */
    @TableId(type = IdType.AUTO)
    private Integer activeId;
    /**
     * 评审名称
     */
    private String reviewName;
    /**
     * 评审目的
     */
    private String reviewPurpose;
    /**
     * 评审时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date reviewTime;
    /**
     *评审地点
     */
    private String reviewPlace;
    /**
     *评审主持
     */
    private String reviewHost;
    /**
     *参与人员
     */
    private String participants;
    /**
     *编制人id
     */
    private String editorId;
    /**
     *编制人姓名
     */
    private String editorName;
    /**
     *编制时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date editorDate;
    /**
     *批准人id
     */
    private String approverId;
    /**
     *批准人姓名
     */
    private String approverName;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date approverDate;
    /**
     *状态：待开始，进行中，已完成
     */
    private String state;
    /**
     *管理评审总结,如果多个逗号隔开
     */
    private String fileUrl;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date time;
    /**
     * 内容纲要列表信息
     */
    @TableField(exist = false)
    private List<ActiveContentEntity> list;
}
