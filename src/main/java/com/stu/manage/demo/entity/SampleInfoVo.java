package com.stu.manage.demo.entity;

import java.util.List;

public class SampleInfoVo {
    private String modelId;
    private String objectCount;
    private String productCompanyName;
    private int productId;
    private String productNumber;
    private String productStandardSource;
    private String productName;
    private int sampleType;
    private String sampleTypeName;
    private List<CheckItemInfoVo> checkList;

    public SampleInfoVo() {
    }



    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getObjectCount() {
        return objectCount;
    }

    public void setObjectCount(String objectCount) {
        this.objectCount = objectCount;
    }

    public String getProductCompanyName() {
        return productCompanyName;
    }

    public void setProductCompanyName(String productCompanyName) {
        this.productCompanyName = productCompanyName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getProductStandardSource() {
        return productStandardSource;
    }

    public void setProductStandardSource(String productStandardSource) {
        this.productStandardSource = productStandardSource;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getSampleType() {
        return sampleType;
    }

    public void setSampleType(int sampleType) {
        this.sampleType = sampleType;
    }

    public String getSampleTypeName() {
        return sampleTypeName;
    }

    public void setSampleTypeName(String sampleTypeName) {
        this.sampleTypeName = sampleTypeName;
    }

    public List<CheckItemInfoVo> getCheckList() {
        return checkList;
    }

    public void setCheckList(List<CheckItemInfoVo> checkList) {
        this.checkList = checkList;
    }
}
