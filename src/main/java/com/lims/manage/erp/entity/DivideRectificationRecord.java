package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-07-09 15:58
 * @Copyright © 河南交科院
 */
@Data
@TableName("qs_divide_rectification_record")
public class DivideRectificationRecord {
    @TableField(exist = false)
    private int activeId;
    /**
     * 分工id
     */
    @TableId
    private int divideId;
    /**
     * 原因分析及纠正措施
     */
    private String analysisAndCorrectiveMeasures;
    /**
     * 要求完成日期
     */
    private Date requiredCompletionDate;
    /**
     * 要求完成日期-String
     */
    @TableField(exist = false)
    private String requiredCompletionTime;
    /**
     * 部门负责人
     */
    private String deptLeader;
    /**
     * 实际完成日期
     */
    private Date actualFinishingDate;
    /**
     * 实际完成日期 - string
     */
    @TableField(exist = false)
    private String actualFinishingTime;
    /**
     * 纠正完成情况
     */
    private String correctionCompletionStatus;
    /**
     * 接收日期
     */
    private Date receivedDate;
    /**
     * 接收日期 - string
     */
    @TableField(exist = false)
    private String receivedTime;
    /**
     * 纠正措施的验证
     */
    private String verificationOfCorrectiveMeasures;
    /**
     * 验证日期
     */
    private Date verificationDate;
    /**
     * 验证日期
     */
    @TableField(exist = false)
    private String verificationTime;
    /**
     * 内审员id
     */
    private String auditorId;
    /**
     * 内审员姓名
     */
    private String auditorName;
    /**
     * 附件url多个逗号分隔
     */
    private String url;
    /**
     *状态：整改通知,等待纠正,等待验证,已完成
     */
    private String state;

}
