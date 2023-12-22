package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TestItemOrderWorkingHours implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer checkItemId;
    private String itemName;
    private Integer itemId;
    private String workingHours;
    private Integer times;
    private Date createTime;
    private Date updateTime;
    private String source;
    private Long workingHoursId;
    private Long taskId;
    private Integer sampleId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemId(Integer checkItemId) {
        this.checkItemId = checkItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName == null ? null : itemName.trim();
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours == null ? null : workingHours.trim();
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public Long getWorkingHoursId() {
        return workingHoursId;
    }

    public void setWorkingHoursId(Long workingHoursId) {
        this.workingHoursId = workingHoursId;
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
        TestItemOrderWorkingHours other = (TestItemOrderWorkingHours) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getCheckItemId() == null ? other.getCheckItemId() == null : this.getCheckItemId().equals(other.getCheckItemId()))
                && (this.getItemName() == null ? other.getItemName() == null : this.getItemName().equals(other.getItemName()))
                && (this.getItemId() == null ? other.getItemId() == null : this.getItemId().equals(other.getItemId()))
                && (this.getWorkingHours() == null ? other.getWorkingHours() == null : this.getWorkingHours().equals(other.getWorkingHours()))
                && (this.getTimes() == null ? other.getTimes() == null : this.getTimes().equals(other.getTimes()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
                && (this.getSource() == null ? other.getSource() == null : this.getSource().equals(other.getSource()))
                && (this.getWorkingHoursId() == null ? other.getWorkingHoursId() == null : this.getWorkingHoursId().equals(other.getWorkingHoursId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCheckItemId() == null) ? 0 : getCheckItemId().hashCode());
        result = prime * result + ((getItemName() == null) ? 0 : getItemName().hashCode());
        result = prime * result + ((getItemId() == null) ? 0 : getItemId().hashCode());
        result = prime * result + ((getWorkingHours() == null) ? 0 : getWorkingHours().hashCode());
        result = prime * result + ((getTimes() == null) ? 0 : getTimes().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getSource() == null) ? 0 : getSource().hashCode());
        result = prime * result + ((getWorkingHoursId() == null) ? 0 : getWorkingHoursId().hashCode());
        return result;
    }
}