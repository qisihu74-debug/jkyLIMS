package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@SuppressWarnings("serial")
@TableName("test_billing_registration")
public class TestBillingRegistrationEntity implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Long entrustmentId;

    private Integer entrustmentNo;

    private String entrustCompany;

    private String sampleName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registrationTime;

    private String registeredName;

    private Long registeredUserid;

    private String remark;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date crateTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 开始页
     */
    @TableField(exist = false)
    private Integer pageNum;
    /**
     * 每页最大数
     */
    @TableField(exist = false)
    private Integer pageSize;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getEntrustmentId() {
        return entrustmentId;
    }

    public void setEntrustmentId(Long entrustmentId) {
        this.entrustmentId = entrustmentId;
    }

    public Integer getEntrustmentNo() {
        return entrustmentNo;
    }

    public void setEntrustmentNo(Integer entrustmentNo) {
        this.entrustmentNo = entrustmentNo;
    }

    public String getEntrustCompany() {
        return entrustCompany;
    }

    public void setEntrustCompany(String entrustCompany) {
        this.entrustCompany = entrustCompany == null ? null : entrustCompany.trim();
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName == null ? null : sampleName.trim();
    }

    public Date getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Date registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getRegisteredName() {
        return registeredName;
    }

    public void setRegisteredName(String registeredName) {
        this.registeredName = registeredName == null ? null : registeredName.trim();
    }

    public Long getRegisteredUserid() {
        return registeredUserid;
    }

    public void setRegisteredUserid(Long registeredUserid) {
        this.registeredUserid = registeredUserid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Date getCrateTime() {
        return crateTime;
    }

    public void setCrateTime(Date crateTime) {
        this.crateTime = crateTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TestBillingRegistrationEntity other = (TestBillingRegistrationEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getEntrustmentId() == null ? other.getEntrustmentId() == null : this.getEntrustmentId().equals(other.getEntrustmentId()))
                && (this.getEntrustmentNo() == null ? other.getEntrustmentNo() == null : this.getEntrustmentNo().equals(other.getEntrustmentNo()))
                && (this.getEntrustCompany() == null ? other.getEntrustCompany() == null : this.getEntrustCompany().equals(other.getEntrustCompany()))
                && (this.getSampleName() == null ? other.getSampleName() == null : this.getSampleName().equals(other.getSampleName()))
                && (this.getRegistrationTime() == null ? other.getRegistrationTime() == null : this.getRegistrationTime().equals(other.getRegistrationTime()))
                && (this.getRegisteredName() == null ? other.getRegisteredName() == null : this.getRegisteredName().equals(other.getRegisteredName()))
                && (this.getRegisteredUserid() == null ? other.getRegisteredUserid() == null : this.getRegisteredUserid().equals(other.getRegisteredUserid()))
                && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
                && (this.getCrateTime() == null ? other.getCrateTime() == null : this.getCrateTime().equals(other.getCrateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getEntrustmentId() == null) ? 0 : getEntrustmentId().hashCode());
        result = prime * result + ((getEntrustmentNo() == null) ? 0 : getEntrustmentNo().hashCode());
        result = prime * result + ((getEntrustCompany() == null) ? 0 : getEntrustCompany().hashCode());
        result = prime * result + ((getSampleName() == null) ? 0 : getSampleName().hashCode());
        result = prime * result + ((getRegistrationTime() == null) ? 0 : getRegistrationTime().hashCode());
        result = prime * result + ((getRegisteredName() == null) ? 0 : getRegisteredName().hashCode());
        result = prime * result + ((getRegisteredUserid() == null) ? 0 : getRegisteredUserid().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getCrateTime() == null) ? 0 : getCrateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }
}