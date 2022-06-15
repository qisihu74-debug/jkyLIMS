package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class TeamOutputValueVo {
    private String teamId;
    private String teamName;
    private String teamCode;
    private String taskPrice;
    /**
     * 实收报告产值
     */
    private String actualReportPrice;
    /**
     * 应收报告产值
     */
    private String systemReportPrice;
}
