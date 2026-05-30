package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-07-09 11:15
 * @Copyright © 河南交科院
 */
@Data
@TableName("qs_audit_divide_detail")
public class DivideAuditDetail {
    /**
     * 分工id
     */
    private int divideId;
    /**
     * 目录id
     */
    private int directoryId;
    /**
     * 审核记录
     */
    private String record;
    /**
     * 评审意见
     */
    private String opinion;
    /**
     * 评审发现
     */
    private String findings;
}
