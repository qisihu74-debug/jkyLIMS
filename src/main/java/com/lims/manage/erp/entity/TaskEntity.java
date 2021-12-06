package com.lims.manage.erp.entity;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2021/12/6 16:03
 * @Copyright © 河南交科院
 */
@Data
public class TaskEntity {
    /**
     * 任务id
     */
    private Long id;
    /**
     * 任务编号
     */
    private String code;
    /**
     * 委托单id
     */
    private Long entrustmentId;
    /**
     * 下单人（委托单受理人）
     */
    private String orderer;
    /**
     * 下单时间
     */
    private Timestamp orderTime;
    /**
     * 要求完成时间
     */
    private Timestamp requiredCompletionTime;
    /**
     * 团队id
     */
    private String teamId;
    /**
     * 接收人（团队中的副团长）
     */
    private String receiver;
    /**
     * 接收时间
     */
    private Timestamp receiveTime;
}
