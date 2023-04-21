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
    private Long entrustId;
    private String token;
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
    private Integer sampleId;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 样品编号
     */
    private String sampleCode;
    /**
     * 样品模板地址
     */
    private String producTexcelUrl;
    /**
     * 报告编辑模板
     */
    private String reportEditUrl;
    /**
     * 样品检测项
     */
    private List<SampleItemEntity> sampleCheckItem;
}
