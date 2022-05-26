package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class StatisticsParamVo {
    private String taskSource;
    private String beginDate;
    private String endDate;
    private String teamId;
    private Integer pageNum;
    private Integer pageSize;
}
