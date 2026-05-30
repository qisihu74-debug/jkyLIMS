package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;
@Data
public class UpdateIssueReportVo {
    private Long entrustmentId;
    private List<Integer> deptIds;
}
