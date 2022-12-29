package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class InstrumentRecordListVo {
    private Long recordId;
    private String instrumentCode;
    private String instrumentName;
    private String instrumentModel;
    private String taskCode;
    private String checkItem;
    private String temperature;
    private String humidity;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    private String type;
    private String user;
    private String beforeStatus;
    private String afterStatus;
}
