package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ReserveCodeEntity {
    private Long id;

    private Integer entrustmentNo;

    private String reportCode;

    private String type;

    private String state;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createDate;

    private String remark;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date useDate;

    private Integer pageNum;
    private Integer pageSize;
}
