package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ReviewVo {
    private Date startTime;
    private Date endTime;
    private String result;
    private String originUrl;
}
