package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class UpdateReportTeamVo {
    /**
     * 是否出具报告
     */
    private String issueReport;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 委托单id
     */
    private Long entrustmentId;
}
