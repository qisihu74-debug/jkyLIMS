package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportProgressVo {
    /**
     * 报告编号
     */
    private String reportCode;
    /**
     * 报告状态状态
     */
    private Integer state;
    private Integer type;

    public ReportProgressVo(String reportCode, Integer state,Integer type) {
        this.reportCode = reportCode;
        this.state = state;
        this.type = type;
    }

    public ReportProgressVo(String reportCode) {
        this.reportCode = reportCode;
    }

    public ReportProgressVo() {
    }

    List<ReportProgressStateVo> reportProgressStateList;
}
