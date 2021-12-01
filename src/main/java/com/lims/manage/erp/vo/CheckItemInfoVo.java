package com.lims.manage.erp.vo;

public class CheckItemInfoVo {
    private Integer checkItemId;
    private String checkItemName;
    private Integer productId;
    private String checkPrice;
    private Integer methodId;
    private String methodName;
    private Integer standardId;
    private String standardCode;
    private String standardName;

    public CheckItemInfoVo() {
    }

    public CheckItemInfoVo(Integer checkItemId, String checkItemName, Integer productId, String checkPrice, Integer methodId, String methodName, Integer standardId, String standardCode, String standardName) {
        this.checkItemId = checkItemId;
        this.checkItemName = checkItemName;
        this.productId = productId;
        this.checkPrice = checkPrice;
        this.methodId = methodId;
        this.methodName = methodName;
        this.standardId = standardId;
        this.standardCode = standardCode;
        this.standardName = standardName;
    }

    public Integer getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemId(Integer checkItemId) {
        this.checkItemId = checkItemId;
    }

    public String getCheckItemName() {
        return checkItemName;
    }

    public void setCheckItemName(String checkItemName) {
        this.checkItemName = checkItemName;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getCheckPrice() {
        return checkPrice;
    }

    public void setCheckPrice(String checkPrice) {
        this.checkPrice = checkPrice;
    }

    public Integer getMethodId() {
        return methodId;
    }

    public void setMethodId(Integer methodId) {
        this.methodId = methodId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Integer getStandardId() {
        return standardId;
    }

    public void setStandardId(Integer standardId) {
        this.standardId = standardId;
    }

    public String getStandardCode() {
        return standardCode;
    }

    public void setStandardCode(String standardCode) {
        this.standardCode = standardCode;
    }

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    @Override
    public String toString() {
        return "CheckItemInfoVo{" +
                "checkItemId=" + checkItemId +
                ", checkItemName='" + checkItemName + '\'' +
                ", productId=" + productId +
                ", checkPrice='" + checkPrice + '\'' +
                ", methodId=" + methodId +
                ", methodName='" + methodName + '\'' +
                ", standardId=" + standardId +
                ", standardCode='" + standardCode + '\'' +
                ", standardName='" + standardName + '\'' +
                '}';
    }
}
