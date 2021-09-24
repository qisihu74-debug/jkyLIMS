package com.stu.manage.demo.entity;

import java.util.List;

public class SampleInfoVo {
    private int productId;
    private String productName;
    private String standard;
    private String model;
    private String batch;
    private String sampleCount;
    private String vendor;
    private String sampleType;
    private List<CheckItemInfoVo> items;

    public SampleInfoVo() {
    }

    public SampleInfoVo(SampleInfoVo infoVo, List<CheckItemInfoVo> items) {
        this.productId = infoVo.getProductId();
        this.productName = infoVo.getProductName();
        this.standard = infoVo.getStandard();
        this.model = infoVo.getModel();
        this.batch = infoVo.getBatch();
        this.sampleCount = infoVo.getSampleCount();
        this.vendor = infoVo.getVendor();
        this.sampleType = infoVo.getSampleType();
        this.items = items;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(String sampleCount) {
        this.sampleCount = sampleCount;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public List<CheckItemInfoVo> getItems() {
        return items;
    }

    public void setItems(List<CheckItemInfoVo> items) {
        this.items = items;
    }
}
