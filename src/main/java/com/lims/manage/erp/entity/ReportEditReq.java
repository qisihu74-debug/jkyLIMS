package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2023-04-19 11:21
 * @Copyright © 河南交科院
 */
@Data
public class ReportEditReq {
    private Long taskId;
    /**
     * 0最终报告，1中间报告
     */
    private Integer reportType;
    /**
     * 2暂存，1完成保存
     */
    private Integer reportComplete;
    /**
     * 任务流转id
     */
    private Integer taskFlowId;
    private List<SampleEntity> sampleInfo;
}
