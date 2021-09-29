package com.stu.manage.demo.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc
 * @date 2021/9/23 17:58
 * @Copyright © 河南交科院
 */
@Data
public class SampleStatus {
    private int sampleId;
    /**
     * 样品编号
     */
    private String sampleCode;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 报告状态1.流程审核中2.非正常结束3.审核通过4.待发起
     */
    private String reportStat;
    /**
     * 任务状态1.新建，2.费用已确定，3.检测中，4.检测完成
     */
    private String checkStat;
    /**
     * 任务流程单审批状态：1.流程审核中，2.非正常结束3.审核通过4.待发起
     */
    private String checkNoticeStat;
}
