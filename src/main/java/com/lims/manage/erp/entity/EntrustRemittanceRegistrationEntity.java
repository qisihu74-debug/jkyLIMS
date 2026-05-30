package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@TableName("test_entrust_remittance_registration")
@Data
public class EntrustRemittanceRegistrationEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Long entrustedId;
    private String entrustmentNo;
    /**
     * 回款金额
     */
    private String amount;
    /**
     * 登记时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date registrationDate;
    /**
     * 付款方式
     */
    private String paymentMethod;
    /**
     * 付款来源
     */
    private String paymentSource;
    /**
     * 备注
     */
    private String note;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
    /**
     * 登记人
     */
    private String registrant;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getEntrustedId() {
        return entrustedId;
    }

    public void setEntrustedId(Long entrustedId) {
        this.entrustedId = entrustedId;
    }

    public String getEntrustmentNo() {
        return entrustmentNo;
    }

    public void setEntrustmentNo(String entrustmentNo) {
        this.entrustmentNo = entrustmentNo == null ? null : entrustmentNo.trim();
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount == null ? null : amount.trim();
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod == null ? null : paymentMethod.trim();
    }

    public String getPaymentSource() {
        return paymentSource;
    }

    public void setPaymentSource(String paymentSource) {
        this.paymentSource = paymentSource == null ? null : paymentSource.trim();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? null : note.trim();
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

    public String getRegistrant() {
        return registrant;
    }

    public void setRegistrant(String registrant) {
        this.registrant = registrant == null ? null : registrant.trim();
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
        EntrustRemittanceRegistrationEntity other = (EntrustRemittanceRegistrationEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getEntrustedId() == null ? other.getEntrustedId() == null : this.getEntrustedId().equals(other.getEntrustedId()))
                && (this.getEntrustmentNo() == null ? other.getEntrustmentNo() == null : this.getEntrustmentNo().equals(other.getEntrustmentNo()))
                && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
                && (this.getRegistrationDate() == null ? other.getRegistrationDate() == null : this.getRegistrationDate().equals(other.getRegistrationDate()))
                && (this.getPaymentMethod() == null ? other.getPaymentMethod() == null : this.getPaymentMethod().equals(other.getPaymentMethod()))
                && (this.getPaymentSource() == null ? other.getPaymentSource() == null : this.getPaymentSource().equals(other.getPaymentSource()))
                && (this.getNote() == null ? other.getNote() == null : this.getNote().equals(other.getNote()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
                && (this.getRegistrant() == null ? other.getRegistrant() == null : this.getRegistrant().equals(other.getRegistrant()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getEntrustedId() == null) ? 0 : getEntrustedId().hashCode());
        result = prime * result + ((getEntrustmentNo() == null) ? 0 : getEntrustmentNo().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getRegistrationDate() == null) ? 0 : getRegistrationDate().hashCode());
        result = prime * result + ((getPaymentMethod() == null) ? 0 : getPaymentMethod().hashCode());
        result = prime * result + ((getPaymentSource() == null) ? 0 : getPaymentSource().hashCode());
        result = prime * result + ((getNote() == null) ? 0 : getNote().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getRegistrant() == null) ? 0 : getRegistrant().hashCode());
        return result;
    }
}