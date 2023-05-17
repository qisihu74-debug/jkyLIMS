package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 来自表 test_entrusted_sample_checkitem_rel
 */
@Data
public class CheckItemInfoVo {
    private Integer itemId;
    private Integer checkItemId;
    private String checkItemName;
    private Integer productId;
    private String checkPrice;
    private Integer methodId;
    private String methodName;
    private Integer standardId;
    private String standardCode;
    private String standardName;
    private Integer state;
    private String originUrl;
    private String opinion;
    /***
     * 文件原始附件名称
     */
    private String itemFileName;
    /**
     * 仪器设备
     */
    private List<TestInstrumentEntity> testInstrumentEntityList;
    /**
     * 仪器数组
     */
    private int[] testInstrumentEntityArray;
    /**
     * 开始检测时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date startTime;
    /**
     * 结束检测时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date endTime;
    /**
     * 设备仪器名称
     */
    private String intrusmentName;
    /**
     * 检测样次
     */
    private Integer times;
    /**
     * 检测依据集合
     */
    private List<LabelValueVo> checkBasisList;
    /**
     * 检测项父ID
     */
    private Integer checkItemPid;

    /**
     *  检测项——任务主键
     */
    private Long taskId;

    /**
     * 检测项文件后缀类型
     */
    private String suffixType;

    private String editData;

    public CheckItemInfoVo() {
    }

    public CheckItemInfoVo(Integer checkItemId, String checkItemName, Integer productId, String checkPrice, Integer methodId, String methodName, Integer standardId, String standardCode, String standardName, Integer state,String originUrl,String opinion) {
        this.checkItemId = checkItemId;
        this.checkItemName = checkItemName;
        this.productId = productId;
        this.checkPrice = checkPrice;
        this.methodId = methodId;
        this.methodName = methodName;
        this.standardId = standardId;
        this.standardCode = standardCode;
        this.standardName = standardName;
        this.state = state;
        this.originUrl = originUrl;
        this.opinion = opinion;
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

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getIntrusmentName() {
        return intrusmentName;
    }

    public void setIntrusmentName(String intrusmentName) {
        this.intrusmentName = intrusmentName;
    }

    @Override
    public String toString() {
        return "CheckItemInfoVo{" +
                "itemId=" + itemId +
                ", checkItemId=" + checkItemId +
                ", checkItemName='" + checkItemName + '\'' +
                ", productId=" + productId +
                ", checkPrice='" + checkPrice + '\'' +
                ", methodId=" + methodId +
                ", methodName='" + methodName + '\'' +
                ", standardId=" + standardId +
                ", standardCode='" + standardCode + '\'' +
                ", standardName='" + standardName + '\'' +
                ", state=" + state +
                ", originUrl='" + originUrl + '\'' +
                ", opinion='" + opinion + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", intrusmentName='" + intrusmentName + '\'' +
                ", times=" + times +
                '}';
    }
}
