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
    private String actualPrice;
    /**
     * 交费金额
     */
    private String receivedPrice;
    /**
     * 报告产值
     */
    private String reportPrice;
}
