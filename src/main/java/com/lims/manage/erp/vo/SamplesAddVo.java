package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.SampleEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SamplesAddVo {
    /**
     * 产品ID
     */
    private Integer productId;
    /**
     * 产品名称
     */
    private String sampleName;
    /**
     * 样品名称
     */
    private String aliasName;
    /**
     * 委托单位
     */
    private Integer companyId;
    /**
     * 规格/等级
     */
    private String specs;
    /**
     * 样品要求
     */
    private String sampleRequirement;
    /**
     * 样品类型：做原材检测还是配合比设计检测、配合比验证
     */
    private String sampleType;
    /**
     * 验收员
     */
    private String inspector;
    /**
     * 收样时间
     */
    private Date receivedDate;
    /**
     * 样品备注
     */
    private String sampleRemark;
    /**
     * 设计强度（MPa）
     */
    private String designStrength;
    /**
     * 配制强度（MPa）
     */
    private String intensityConfiguration;
    /**
     * 抗（渗、冻）等级
     */
    private String antifreezeLevel;
    /**
     * 水胶比
     */
    private String waterBinderRatio;
    /**
     * 单位用水量（kg）
     */
    private String unitWaterUse;
    /**
     * 砂率（%）
     */
    private String sandRatio;
    /**
     * 设计坍落度（mm）
     */
    private String designSlump;
    /**
     * 拌和方式
     */
    private String mixingWay;
    /**
     * 单位比
     */
    private String unitRatio;
    /**
     * 每立方米用量
     */
    private String cubicMeterConsumption;
    private List<SampleDetailAddVo> samples;

    public SamplesAddVo() {
    }

    public SamplesAddVo(SampleEntity samples) {
        this.sampleName = samples.getSampleName();
        this.productId = samples.getProductId();
        this.aliasName = samples.getAliasName();
        this.companyId = samples.getCompanyId();
        this.specs = samples.getSpecs();
        this.inspector = samples.getInspector();
        this.sampleRequirement = samples.getSampleRequirement();
        this.sampleType = samples.getSampleType();
        this.sampleRemark = samples.getSampleRemark();
        this.unitRatio = samples.getUnitRatio();
        this.cubicMeterConsumption = samples.getCubicMeterConsumption();
//        this.quantityPerGroup = samples.getQuantityPerGroup();
        this.receivedDate = samples.getCheckDate();
    }

    //用于绑定之前原材
    private List<Integer> sampleIds;
}
