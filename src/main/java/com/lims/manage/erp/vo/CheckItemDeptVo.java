package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.Date;
@Data
public class CheckItemDeptVo {
    private Integer id;
    private Long deptId;
    private Long taskId;
    private Date distributionDate;
    private String issueReport;
}
