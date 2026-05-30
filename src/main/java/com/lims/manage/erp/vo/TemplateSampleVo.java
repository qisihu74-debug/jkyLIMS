package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.SampleEntity;
import lombok.Data;

@Data
public class TemplateSampleVo {
    private String sampleName;
    private String sampleNumber;
    private String sampleQuantity;
    private String sampleDesc;
    private String sampleTime;
    private String sampleType;
    /**
     * 样品备注信息
     */
    private String outwardDescribe;

    /**
     * 规格等级
     */
    private String specs;

//    public TemplateSampleVo() {
//    }
//
//    public TemplateSampleVo(SampleEntity entity) {
//        this.sampleName = entity.getSampleName() == null ? "——" : entity.getSampleName();
//        this.sampleNumber = entity.getSampleCode() == null ? "——" : entity.getSampleCode();
//        this.sampleQuantity = entity.getQuantityPerGroup() == null ? "——" : entity.getQuantityPerGroup()+"";
//        this.sampleDesc = entity.getOutward() == null ? "——" : entity.getOutward();
//        this.sampleTime = entity.getReceivedDate() == null ? "——" :entity.getReceivedDate();
//    }
}
