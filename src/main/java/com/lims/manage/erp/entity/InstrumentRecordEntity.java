package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class InstrumentRecordEntity {
    private Long id;

    private Long instrumentId;

    private Long escRelId;

    private String type;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss" , timezone ="GMT+8")
    private Date startTime;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss" , timezone ="GMT+8")
    private Date endTime;

    private String temperature;

    private String humidity;

    private String beforeStatus;

    private String afterStatus;

    private String user;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss" , timezone ="GMT+8")
    private Date time;
    /**
     * 任务单id
     */
    private Long taskId;
}
