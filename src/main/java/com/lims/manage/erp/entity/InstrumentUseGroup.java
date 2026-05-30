package com.lims.manage.erp.entity;

import lombok.Data;

@Data
public class InstrumentUseGroup {
    private Long instrumentId;
    private String user;
    private Long taskId;
    private String taskCode;
    private Integer state;
    private Integer parallel;
    private String escRelIds;
}
