package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2024/6/11 11:10
 * 报告清单详情表
 */
@Data
public class StatisticsNodeDetailVo {
    /**
     * id
     */
    private Long id;
    /**
     * 委托单id
     */
    private String entrustmentId;
    /**
     * 报告号
     */
    private String reportCode;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 折扣率
     */
    private String discount;
    /**
     * 应收价格
     */
    private String actualPrice;
    /**
     * 实际收费
     */
    private String systemPrice;
    /**
     * 委托单号
     */
    private String entrustmentNo;
    /**
     * 团队信息
     */
    private String teamName;
    /**
     * 任务单号
     */
    private String taskCode;
}
