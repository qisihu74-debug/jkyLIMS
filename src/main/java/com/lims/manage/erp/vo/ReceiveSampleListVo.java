package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

/**
 * 存放领样列表
 */
@Data
public class ReceiveSampleListVo {
    private Long taskId;
    private String taskCode;
    private String requiredTime;
    private String receiveTime;
    private String sampleName;
    private List<SamplePrivateInfoVo> sampleList;
}
