package com.lims.manage.erp.entity;

import com.lims.manage.erp.vo.SamplesAddVo;
import lombok.Data;

@Data
public class TestSampleMixInfoEntity {
    private Integer id;

    private Integer sampleId;

    private Long entrustmentId;

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

    public TestSampleMixInfoEntity(Integer id, Integer sampleId, Long entrustmentId, String designStrength, String intensityConfiguration, String antifreezeLevel, String waterBinderRatio, String unitWaterUse, String sandRatio, String designSlump, String mixingWay) {
        this.id = id;
        this.sampleId = sampleId;
        this.entrustmentId = entrustmentId;
        this.designStrength = designStrength;
        this.intensityConfiguration = intensityConfiguration;
        this.antifreezeLevel = antifreezeLevel;
        this.waterBinderRatio = waterBinderRatio;
        this.unitWaterUse = unitWaterUse;
        this.sandRatio = sandRatio;
        this.designSlump = designSlump;
        this.mixingWay = mixingWay;
    }

    public TestSampleMixInfoEntity(SamplesAddVo samples, Integer sampleId) {
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

    public TestSampleMixInfoEntity(TestSampleEntity samples) {
        this.sampleId = samples.getId();
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