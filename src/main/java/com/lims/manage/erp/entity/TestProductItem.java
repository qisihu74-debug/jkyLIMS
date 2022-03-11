package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 产品检测项(TestProductItem)表实体类
 *
 * @author makejava
 * @since 2022-03-02 15:14:51
 */
@SuppressWarnings("serial")
public class TestProductItem extends Model<TestProductItem> {
    //检验项目ID
    @TableId(type = IdType.AUTO)
    private Integer checkItemId;
    //
    private Integer checkItemPid;
    //检测项编号
    private String checkItemCode;
    //产品ID
    private Integer productId;
    //报告模板id
    private String reportModelId;
    //检验项目名称
    private String checkItemName;
    //检测项在报告中的位置坐标
    private String coordinate;
    //检测项图标
    private String icon;
    //附加费用
    private String additionalFees;
    //检测项单位（1点，2样）
    private String unit;
    //取样数量
    private Integer sampleCount;
    //实验地点
    private String place;
    //实验检测时长
    private String duration;
    //取样频率
    private String frequency;
    //技术难度
    private String difficulty;
    //备注
    private String remark;
    //检测价格
    private String checkPrice;
    //是否有效
    private String isAvailable;
    // 0,启用，1,冻结
    private String status;
    //0默认未删除,1删除
    private Integer delFlag;
    //注册时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    //物流费用
    private String logisticsCosts;

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
        this.checkItemCode = checkItemCode;
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
        this.reportModelId = reportModelId;
    }

    public String getCheckItemName() {
        return checkItemName;
    }

    public void setCheckItemName(String checkItemName) {
        this.checkItemName = checkItemName;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAdditionalFees() {
        return additionalFees;
    }

    public void setAdditionalFees(String additionalFees) {
        this.additionalFees = additionalFees;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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
        this.place = place;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCheckPrice() {
        return checkPrice;
    }

    public void setCheckPrice(String checkPrice) {
        this.checkPrice = checkPrice;
    }

    public String getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(String isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getLogisticsCosts() {
        return logisticsCosts;
    }

    public void setLogisticsCosts(String logisticsCosts) {
        this.logisticsCosts = logisticsCosts;
    }

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.checkItemId;
    }
    }

