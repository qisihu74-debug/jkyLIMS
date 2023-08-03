package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022-12-01 15:28
 * @Copyright © 河南交科院
 */
@Data
public class SampleCirculationRecord {
    private int sampleId;
    /**
     * 状态，1待检，2在检，3已检，4留样，5处置
     */
    private String status;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date time;
    private Long operatorId;
    private String operatorName;
    /**
     * 操作内容 待检（流转确认人：）
     */
    private String content;
}
