package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Date;

public class SampleEntity {
    /**
     *主键id
     */
    private Integer id;
    /**
     *委托单位
     */
    private Integer companyId;
    /**
     *样品名称
     */
    private String sampleName;
    /**
     *样品编号
     */
    private String sampleCode;
    /**
     *规格/等级
     */
    private Integer specs;
    /**
     *批号、编号
     */
    private String batchNumber;
    /**
     *生产厂家
     */
    private String manufacturer;
    /**
     *样品产地
     */
    private String sampleOrigin;
    /**
     *外观
     */
    private String outward;
    /**
     *样品照片
     */
    private String picture;
    /**
     *保存地点
     */
    private String savePlace;
    /**
     *管理员
     */
    private String admin;
    /**
     *样品组数
     */
    private Integer sampleGroups;
    /**
     *每组样品数
     */
    private Integer quantityPerGroup;
    /**
     *验收员
     */
    private String inspector;
    /**
     *接收日期
     */
    private String receivedDate;
    /**
     *样品要求
     */
    private String sampleRequirement;
    /**
     *代表批量
     */
    private String generation;
    /**
     *样品状态
     */
    private String state;
    /**
     *检验时间
     */
    private Date checkDate;
    /**
     *备注
     */
    private String remark;

    public SampleEntity(Integer id, Integer companyId, String sampleName, String sampleCode, Integer specs, String batchNumber, String manufacturer, String sampleOrigin, String outward, String picture, String 
savePlace, String admin, Integer sampleGroups, Integer quantityPerGroup, String inspector, String receivedDate, String sampleRequirement, String generation, String state, Date checkDate, String remark) {
        this.id = id;
        this.companyId = companyId;
        this.sampleName = sampleName;
        this.sampleCode = sampleCode;
        this.specs = specs;
        this.batchNumber = batchNumber;
        this.manufacturer = manufacturer;
        this.sampleOrigin = sampleOrigin;
        this.outward = outward;
        this.picture = picture;
        this.savePlace = savePlace;
        this.admin = admin;
        this.sampleGroups = sampleGroups;
        this.quantityPerGroup = quantityPerGroup;
        this.inspector = inspector;
        this.receivedDate = receivedDate;
        this.sampleRequirement = sampleRequirement;
        this.generation = generation;
        this.state = state;
        this.checkDate = checkDate;
        this.remark = remark;
    }

    public SampleEntity() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName == null ? null : sampleName.trim();
    }

    public String getSampleCode() {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode) {
        this.sampleCode = sampleCode == null ? null : sampleCode.trim();
    }

    public Integer getSpecs() {
        return specs;
    }

    public void setSpecs(Integer specs) {
        this.specs = specs;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer == null ? null : manufacturer.trim();
    }

    public String getSampleOrigin() {
        return sampleOrigin;
    }

    public void setSampleOrigin(String sampleOrigin) {
        this.sampleOrigin = sampleOrigin == null ? null : sampleOrigin.trim();
    }

    public String getOutward() {
        return outward;
    }

    public void setOutward(String outward) {
        this.outward = outward == null ? null : outward.trim();
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture == null ? null : picture.trim();
    }

    public String getsavePlace() {
        return savePlace;
    }

    public void setsavePlace(String savePlace) {
        this.savePlace = savePlace == null ? null : savePlace.trim();
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin == null ? null : admin.trim();
    }

    public Integer getSampleGroups() {
        return sampleGroups;
    }

    public void setSampleGroups(Integer sampleGroups) {
        this.sampleGroups = sampleGroups;
    }

    public Integer getQuantityPerGroup() {
        return quantityPerGroup;
    }

    public void setQuantityPerGroup(Integer quantityPerGroup) {
        this.quantityPerGroup = quantityPerGroup;
    }

    public String getInspector() {
        return inspector;
    }

    public void setInspector(String inspector) {
        this.inspector = inspector == null ? null : inspector.trim();
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate == null ? null : receivedDate.trim();
    }

    public String getSampleRequirement() {
        return sampleRequirement;
    }

    public void setSampleRequirement(String sampleRequirement) {
        this.sampleRequirement = sampleRequirement == null ? null : sampleRequirement.trim();
    }

    public String getGeneration() {
        return generation;
    }

    public void setGeneration(String generation) {
        this.generation = generation == null ? null : generation.trim();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state == null ? null : state.trim();
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }
}