package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class TestTaskOrderWorkingHours implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 任务单id
     */
    private Long taskId;
    /**
     * 任务单号
     */
    private String taskCode;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 总工时
     */
    private String totalWorkingHours;
    /**
     * 检测类型
     */
    private String detectionType;
    /**
     * userId
     */
    private Long userId;
    /**
     * userName
     */
    private String userName;
    /**
     * 使用工时
     */
    private String workingHours;
    /**
     * 比例
     */
    private String proportion;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
    /**
     * 新增操作人
     */
    private String addOperator;
    /**
     * 任务单状态：
     */
    @TableField(exist = false)
    private Integer state;
    /**
     * 来源
     */
    private String source;

    private String workingHoursId;
    /**
     * 折扣率
     */
    private String discount;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTotalWorkingHours() {
        return totalWorkingHours;
    }

    public void setTotalWorkingHours(String totalWorkingHours) {
        this.totalWorkingHours = totalWorkingHours;
    }

    public String getDetectionType() {
        return detectionType;
    }

    public void setDetectionType(String detectionType) {
        this.detectionType = detectionType == null ? null : detectionType.trim();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getProportion() {
        return proportion;
    }

    public void setProportion(String proportion) {
        this.proportion = proportion == null ? null : proportion.trim();
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
        TestTaskOrderWorkingHours other = (TestTaskOrderWorkingHours) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getTaskId() == null ? other.getTaskId() == null : this.getTaskId().equals(other.getTaskId()))
                && (this.getTotalWorkingHours() == null ? other.getTotalWorkingHours() == null : this.getTotalWorkingHours().equals(other.getTotalWorkingHours()))
                && (this.getDetectionType() == null ? other.getDetectionType() == null : this.getDetectionType().equals(other.getDetectionType()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getUserName() == null ? other.getUserName() == null : this.getUserName().equals(other.getUserName()))
                && (this.getWorkingHours() == null ? other.getWorkingHours() == null : this.getWorkingHours().equals(other.getWorkingHours()))
                && (this.getProportion() == null ? other.getProportion() == null : this.getProportion().equals(other.getProportion()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTaskId() == null) ? 0 : getTaskId().hashCode());
        result = prime * result + ((getTotalWorkingHours() == null) ? 0 : getTotalWorkingHours().hashCode());
        result = prime * result + ((getDetectionType() == null) ? 0 : getDetectionType().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
        result = prime * result + ((getWorkingHours() == null) ? 0 : getWorkingHours().hashCode());
        result = prime * result + ((getProportion() == null) ? 0 : getProportion().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    public TestTaskOrderWorkingHours() {
    }

    public TestTaskOrderWorkingHours(TestTaskOrderWorkingHours taskOrderWorkingHours) {
        this.id = taskOrderWorkingHours.getId();
        this.taskId = taskOrderWorkingHours.getTaskId();
        this.taskCode = taskOrderWorkingHours.getTaskCode();
        this.sampleName = taskOrderWorkingHours.getSampleName();
        this.totalWorkingHours = taskOrderWorkingHours.getTotalWorkingHours();
        this.detectionType = taskOrderWorkingHours.getDetectionType();
        this.userId = taskOrderWorkingHours.getUserId();
        this.userName = taskOrderWorkingHours.getUserName();
        this.workingHours = taskOrderWorkingHours.getWorkingHours();
        this.proportion = taskOrderWorkingHours.getProportion();
        this.createTime = taskOrderWorkingHours.getCreateTime();
        this.updateTime = taskOrderWorkingHours.getUpdateTime();
        this.addOperator = taskOrderWorkingHours.getAddOperator();
        this.state = taskOrderWorkingHours.getState();
        this.source = taskOrderWorkingHours.getSource();
        this.workingHoursId = taskOrderWorkingHours.getWorkingHoursId();
        this.discount = taskOrderWorkingHours.getDiscount();
    }


}