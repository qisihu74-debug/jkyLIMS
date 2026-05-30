package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.Date;

/**
 * 领样参数
 */
@Data
public class ReceiveSampleParamVo {
    /**
     * 任务主键ID
     */
    private Long taskId;
    /**
     * 样品ID
     */
    private Integer sampleId;
    /**
     * 领样人
     */
    private String sampler;
    /**
     * 领样时间
     */
    private Date sampleReceivingTime;

    private Integer state;
    /**
     * 样品状态描述
     */
    private String sampleStateDescription;
}
