package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 仪器设备(TestInstrument)表实体类
 *
 * @author makejava
 * @since 2022-02-25 10:05:51
 */
@SuppressWarnings("serial")
public class TestInstrument extends Model<TestInstrument> {
    //仪器id
    @TableId(type = IdType.AUTO)
    private Integer id;
    //仪器大类id
    private Integer typeId;
    //仪器名称
    @TableField("`name`")
    private String name;
    //所在实验室
    private Integer laboratoryId;
    //生产编号
    @TableField("`code`")
    private String code;
    //旧编号
    private String oldCode;
    //生产厂家
    private String manufacturer;
    //规格型号
    private String model;
    //出厂编号
    private String serialNumber;
    //购置价格
    private String price;
    //仪器图片
    private String picture;
    //入编日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date incorporatedDate;
    //最新一次鉴定日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date appraisalDate;
    //有无串口0没有1有
    private String isPort;
    //精度等级
    @TableField("`level`")
    private String level;
    //有效期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date validityDate;
    //服务内容
    private String content;
    //检测使用仪器一次的费用
    private String checkPrice;
    //规格类型，扩展不确定度、最大允差、准确度等级
    private String specs;
    //仪器量程
    @TableField("`range`")
    private String range;
    //合同url
    private String contractUrl;
    //发票url
    private String invoiceUrl;
    //备注
    private String remark;
    //设备管理员
    private String deviceAdmin;
    //小时单价
    private String pricePerHour;
    //溯源方式，送校、内部校验、送检、比对、标准物质、其它
    private String traceabilityMode;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLaboratoryId() {
        return laboratoryId;
    }

    public void setLaboratoryId(Integer laboratoryId) {
        this.laboratoryId = laboratoryId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOldCode() {
        return oldCode;
    }

    public void setOldCode(String oldCode) {
        this.oldCode = oldCode;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Date getIncorporatedDate() {
        return incorporatedDate;
    }

    public void setIncorporatedDate(Date incorporatedDate) {
        this.incorporatedDate = incorporatedDate;
    }

    public Date getAppraisalDate() {
        return appraisalDate;
    }

    public void setAppraisalDate(Date appraisalDate) {
        this.appraisalDate = appraisalDate;
    }

    public String getIsPort() {
        return isPort;
    }

    public void setIsPort(String isPort) {
        this.isPort = isPort;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Date getValidityDate() {
        return validityDate;
    }

    public void setValidityDate(Date validityDate) {
        this.validityDate = validityDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCheckPrice() {
        return checkPrice;
    }

    public void setCheckPrice(String checkPrice) {
        this.checkPrice = checkPrice;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getContractUrl() {
        return contractUrl;
    }

    public void setContractUrl(String contractUrl) {
        this.contractUrl = contractUrl;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDeviceAdmin() {
        return deviceAdmin;
    }

    public void setDeviceAdmin(String deviceAdmin) {
        this.deviceAdmin = deviceAdmin;
    }

    public String getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(String pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public String getTraceabilityMode() {
        return traceabilityMode;
    }

    public void setTraceabilityMode(String traceabilityMode) {
        this.traceabilityMode = traceabilityMode;
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

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }
    }

