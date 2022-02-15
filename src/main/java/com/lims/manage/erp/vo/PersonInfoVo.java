package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class PersonInfoVo {
    private Long taskId;
    private String inspector;
    private String recorder;
    private String reviewer;
    private String reportProducer;
}
