package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class AreaStatisticsResultVo {
    /**
     * 任务来源
     */
    private String taskSource;
    /**
     * 委托收费额
     */
    private Double actualPrice;
    /**
     * 交费金额
     */
    private Double receivedPrice;
    /**
     * 报告实收产值
     */
    private Double actualReportPrice;
    /**
     * 报告应收产值
     */
    private Double systemReportPrice;
}
