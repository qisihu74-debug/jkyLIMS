package com.lims.manage.erp.entity;

import com.lims.manage.erp.vo.SamplesAddVo;
import lombok.Data;

@Data
public class TestSampleMixInfoEntity {
    private Integer id;

    private Integer sampleId;

    private Integer entrustmentId;

    private String designStrength;

    private String intensityConfiguration;

    private String antifreezeLevel;

    private String waterBinderRatio;

    private String unitWaterUse;

    private String sandRatio;

    private String designSlump;

    private String mixingWay;

    public TestSampleMixInfoEntity() {
    }

    public TestSampleMixInfoEntity(SamplesAddVo samples,Integer sampleId) {
        this.sampleId = sampleId;
        this.designStrength = samples.getDesignStrength();
        this.intensityConfiguration = samples.getIntensityConfiguration();
        this.antifreezeLevel = samples.getAntifreezeLevel();
        this.waterBinderRatio = samples.getWaterBinderRatio();
        this.unitWaterUse = samples.getUnitWaterUse();
        this.sandRatio = samples.getSandRatio();
        this.designSlump = samples.getDesignSlump();
        this.mixingWay = samples.getMixingWay();
    }
}