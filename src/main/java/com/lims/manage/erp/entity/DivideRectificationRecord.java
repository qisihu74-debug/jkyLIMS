package com.lims.manage.erp.entity;

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
    /**
     * 分工id
     */
    @TableId
    private int divideId;
    /**
     *
     */
    private String analysisAndCorrectiveMeasures;
    private Date requiredCompletionDate;
    private String deptLeader;
    private Date actualFinishingDate;
    private String correctionCompletionStatus;
    private Date receivedDate;
    private String verificationOfCorrectiveMeasures;
    private Date verificationDate;
    private String auditorId;
    private String auditorName;
    private String url;
    private String state;

}
