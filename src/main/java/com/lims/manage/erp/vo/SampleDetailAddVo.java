package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SampleDetailAddVo {
    private Integer id;
    private String key;
    /**
     * 产品名称
     */
    private String sampleName;
    /**
     * 样品编号
     */
    private String sampleCode;
    /**
     * 产品ID
     */
    private Integer productId;
    /**
     * 样品名称
     */
    private String aliasName;
    /**
     * 委托单位
     */
    private Integer companyId;
    /**
     * 样品产地
     */
    private String sampleOrigin;
    /**
     * 生产厂家
     */
    private String manufacturer;
    /**
     * 规格/等级
     */
    private String specs;
    /**
     * 代表批量
     */
    private String generation;
    /**
     * 样品数量
     */
    private String sampleQuantity;
    /**
     * 每组样品数量
     */
    private Integer quantityPerGroup;
    /**
     * 批号编号
     */
    private String batchNumber;
    /**
     * 样品备注
     */
    private String sampleRemark;
    /**
     * 验收员
     */
    private String inspector;
    /**
     * 收样时间
     */
    private Date receivedDate;
    /**
     * 样品要求
     */
    private String sampleRequirement;
    /**
     * 外观描述
     */
    private String outwardDescribe;
    /**
     * 外观
     */
    private List<String> outward;
    /**
     * 样品类型：做原材检测还是配合比检测
     */
    private String sampleType;
    /**
     * 单位比
     */
    private String unitRatio;
    /**
     * 每立方米用量
     */
    private String cubicMeterConsumption;
    /**
     * 原材的父ID
     */
    private Integer pid;

    public SampleDetailAddVo() {
    }

    public SampleDetailAddVo(SamplesAddVo samples,Integer id) {
        this.id = id;
        this.sampleName = samples.getSampleName();
        this.productId = samples.getProductId();
        this.aliasName = samples.getAliasName();
        this.companyId = samples.getCompanyId();
        this.specs = samples.getSpecs();
        this.inspector = samples.getInspector();
        this.receivedDate = samples.getReceivedDate();
        this.sampleRequirement = samples.getSampleRequirement();
        this.sampleType = samples.getSampleType();
        this.sampleRemark = samples.getSampleRemark();
        this.unitRatio = samples.getUnitRatio();
        this.cubicMeterConsumption = samples.getCubicMeterConsumption();
    }
}
