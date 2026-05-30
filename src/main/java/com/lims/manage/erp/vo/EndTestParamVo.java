package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class EndTestParamVo {
    private List<Integer> itemInstrumentEntityList;
    private String result;
    private Date endTime;
    private Long taskId;
}
