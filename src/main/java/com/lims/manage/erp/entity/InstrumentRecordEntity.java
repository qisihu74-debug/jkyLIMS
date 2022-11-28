package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;

@Data
public class InstrumentRecordEntity {
    private Long id;

    private Long instrumentId;

    private Long escRelId;

    private String type;

    private Date startTime;

    private Date endTime;

    private String temperature;

    private String humidity;

    private String beforeStatus;

    private String afterStatus;

    private String user;

    private Date time;
}
