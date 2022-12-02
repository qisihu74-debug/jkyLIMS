package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lims.manage.erp.vo.SampleDetailAddVo;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
@TableName("test_sample")
public class TestSampleEntity {
    /**
     * 扫码操作类型1.查询，2领样，3留样，4处置
     */
    @TableField(exist = false)
    private List<Long> operateType;
    /**
     * 流转记录
     */
    @TableField(exist = false)
    private List<SampleCirculationRecord> circulationCecords;
    private Integer id;

    private Integer companyId;

    private String sampleName;

    private String sampleCode;

    private String specs;

    private String batchNumber;

    private String manufacturer;

    private String sampleOrigin;

    private String outward;

    private String savePlace;

    private String admin;

    private Integer sampleGroups;

    private Integer quantityPerGroup;

    private String inspector;

    private String receivedDate;

    private String sampleRequirement;

    private String generation;
    /**
     * 样品状态：待检0；领样1；在检2；已检3；
     */
    private String state;
    /**
     * 是否留样1.保留2.处置
     */
    private String isSave;

    private Date checkDate;

    private String remark;
    /**
     * 文件url
     */
    private String file;
    /**
     * 文件url原始名称
     */
    private String fileUrlStr;

    private Integer productId;

    private String outwardDescribe;

    private String insertFlag;

    private Integer isUse;

    private String sampleQuantity;

    private String aliasName;

    private String sampleType;

    private String sampleRemark;

    private String picture;
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

    @TableField(exist = false)
    private Integer pageNum;
    @TableField(exist = false)
    private Integer pageSize;
    @TableField(exist = false)
    private String beginDate;
    @TableField(exist = false)
    private String endDate;
    @TableField(exist = false)
    private String companyName;
    @TableField(exist = false)
    private List<String> outwardArr;
    /**
     * 样品文件集合。
     */
    @TableField(exist = false)
    private List<TestSampleCollectionJSON> fileArrays;
    /**
     * 子样品信息
     */
    @TableField(exist = false)
    private List<TestSampleEntity> nodeSample;
    /**
     * 配合比信息
     */
    @TableField(exist = false)
    private TestSampleMixInfoEntity mixInfo;


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





    public TestSampleEntity() {
    }

    public TestSampleEntity(SampleDetailAddVo vo,String sampleCode,String outward) {
        this.id = vo.getId();
        this.companyId = vo.getCompanyId();
        this.sampleName = vo.getSampleName();
        this.sampleCode = sampleCode;
        this.specs = vo.getSpecs();
        this.batchNumber = vo.getBatchNumber();
        this.manufacturer = vo.getManufacturer();
        this.sampleOrigin = vo.getSampleOrigin();
        this.outward = outward;
        this.inspector = vo.getInspector();
        this.receivedDate = new SimpleDateFormat("yyyy-MM-dd").format(vo.getReceivedDate());
        this.sampleRequirement = vo.getSampleRequirement();
        this.generation = vo.getGeneration();
        this.state = 0+"";
        this.productId = vo.getProductId();
        this.outwardDescribe = vo.getOutwardDescribe();
        this.isUse = 0;
        this.sampleQuantity = vo.getSampleQuantity();
        this.quantityPerGroup = vo.getQuantityPerGroup();
        this.aliasName = vo.getAliasName();
        this.sampleType = vo.getSampleType();
        this.sampleRemark = vo.getSampleRemark();
        this.unitRatio = vo.getUnitRatio();
        this.cubicMeterConsumption = vo.getCubicMeterConsumption();
        this.pid = vo.getPid();
    }
}
