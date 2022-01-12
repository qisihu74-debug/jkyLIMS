package com.lims.manage.erp.service;

import com.lims.manage.erp.vo.ReportListVo;

import java.util.List;

public interface ReportService {
    /**
     * 查询可制作报告列表
     *
     * @return
     */
    List<ReportListVo> getReportList();
}
