package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

/**
 * 同一批次样品通用信息
 */
@Data
public class SamplePublicInfoVo {
    private Integer companyId;
    private String companyName;
    private String sampleName;
    private String specs;
    private String manufacturer;
    private String sampleOrigin;
    private String outward;
    private Integer sampleGroups;
    private Integer quantityPerGroup;
    private String inspector;
    private String receivedDate;
    private String sampleRequirement;
    private String generation;
    private Integer productId;
    private String insertFlag;
    private List<SamplePrivateInfoVo> childNode;
}
