package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ReportProgressStateVo {
    /**
     * 报告状态名称
     */
    private String title;
    /**
     * 状态时间
     */
    private Date time;
}
