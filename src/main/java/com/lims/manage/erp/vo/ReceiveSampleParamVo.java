package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.Date;

/**
 * 领样参数
 */
@Data
public class ReceiveSampleParamVo {
    private Long taskId;
    private String sampler;
    private Date sampleReceivingTime;
    private Integer state;
}
