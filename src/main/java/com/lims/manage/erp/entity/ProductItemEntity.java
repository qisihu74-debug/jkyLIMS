package com.lims.manage.erp.entity;

public class ProductItemEntity {
    private Integer checkItemId;

    private Integer checkItemPid;

    private String checkItemCode;

    private Integer productId;

    private String reportModelId;

    private String checkItemName;

    private String coordinate;

    private String icon;

    private String additionalFees;

    private String logisticsCosts;

    private String unit;

    private String samplestandard;

    private Integer sampleCount;

    private String place;

    private String duration;

    private String frequency;

    private String difficulty;

    private String remark;

    public ProductItemEntity(Integer checkItemId, Integer checkItemPid, String checkItemCode, Integer productId, String reportModelId, String checkItemName, String coordinate, String icon, String additionalFees, String 
logisticsCosts, String unit, String samplestandard, Integer sampleCount, String place, String duration, String frequency, String difficulty, String remark) {
        this.checkItemId = checkItemId;
        this.checkItemPid = checkItemPid;
        this.checkItemCode = checkItemCode;
        this.productId = productId;
        this.reportModelId = reportModelId;
        this.checkItemName = checkItemName;
        this.coordinate = coordinate;
        this.icon = icon;
        this.additionalFees = additionalFees;
        this.logisticsCosts = logisticsCosts;
        this.unit = unit;
        this.samplestandard = samplestandard;
        this.sampleCount = sampleCount;
        this.place = place;
        this.duration = duration;
        this.frequency = frequency;
        this.difficulty = difficulty;
        this.remark = remark;
    }

    public ProductItemEntity() {
        super();
    }

    public Integer getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemId(Integer checkItemId) {
        this.checkItemId = checkItemId;
    }

    public Integer getCheckItemPid() {
        return checkItemPid;
    }

    public void setCheckItemPid(Integer checkItemPid) {
        this.checkItemPid = checkItemPid;
    }

    public String getCheckItemCode() {
        return checkItemCode;
    }

    public void setCheckItemCode(String checkItemCode) {
        this.checkItemCode = checkItemCode == null ? null : checkItemCode.trim();
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getReportModelId() {
        return reportModelId;
    }

    public void setReportModelId(String reportModelId) {
        this.reportModelId = reportModelId == null ? null : reportModelId.trim();
    }

    public String getCheckItemName() {
        return checkItemName;
    }

    public void setCheckItemName(String checkItemName) {
        this.checkItemName = checkItemName == null ? null : checkItemName.trim();
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate == null ? null : coordinate.trim();
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon == null ? null : icon.trim();
    }

    public String getAdditionalFees() {
        return additionalFees;
    }

    public void setAdditionalFees(String additionalFees) {
        this.additionalFees = additionalFees == null ? null : additionalFees.trim();
    }

    public String getlogisticsCosts() {
        return logisticsCosts;
    }

    public void setlogisticsCosts(String logisticsCosts) {
        this.logisticsCosts = logisticsCosts == null ? null : logisticsCosts.trim();
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit == null ? null : unit.trim();
    }

    public String getSamplestandard() {
        return samplestandard;
    }

    public void setSamplestandard(String samplestandard) {
        this.samplestandard = samplestandard == null ? null : samplestandard.trim();
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place == null ? null : place.trim();
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration == null ? null : duration.trim();
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency == null ? null : frequency.trim();
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty == null ? null : difficulty.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }
}