package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@SuppressWarnings("serial")
@TableName("manage_review_plan")
public class ManageReviewPlanEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String planCreator;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planCreationTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date reviewStartTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date reviewEndTime;
    private String reviewPurpose;
    private String reviewHost;
    private String reviewLocation;
    private String reviewReportStatus;
    private Integer delFlag;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
    /**
     * 参与人员
     */
    private String participant;
    /**
     * 参与人员
     */
    @TableField(exist = false)
    private List<ManageReviewInformationEntity> manageReviewInformationEntities = new ArrayList<>();
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
    /**
     * 操作标记 sign = 0 可操作，为null或！=0 不可操作
     */
    @TableField(exist = false)
    private Integer sign;

    private String originalFileName;

    private String fileUrl;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlanCreator() {
        return planCreator;
    }

    public void setPlanCreator(String planCreator) {
        this.planCreator = planCreator == null ? null : planCreator.trim();
    }

    public Date getPlanCreationTime() {
        return planCreationTime;
    }

    public void setPlanCreationTime(Date planCreationTime) {
        this.planCreationTime = planCreationTime;
    }

    public Date getReviewStartTime() {
        return reviewStartTime;
    }

    public void setReviewStartTime(Date reviewStartTime) {
        this.reviewStartTime = reviewStartTime;
    }

    public Date getReviewEndTime() {
        return reviewEndTime;
    }

    public void setReviewEndTime(Date reviewEndTime) {
        this.reviewEndTime = reviewEndTime;
    }

    public String getReviewPurpose() {
        return reviewPurpose;
    }

    public void setReviewPurpose(String reviewPurpose) {
        this.reviewPurpose = reviewPurpose == null ? null : reviewPurpose.trim();
    }

    public String getReviewHost() {
        return reviewHost;
    }

    public void setReviewHost(String reviewHost) {
        this.reviewHost = reviewHost == null ? null : reviewHost.trim();
    }

    public String getReviewLocation() {
        return reviewLocation;
    }

    public void setReviewLocation(String reviewLocation) {
        this.reviewLocation = reviewLocation == null ? null : reviewLocation.trim();
    }

    public String getReviewReportStatus() {
        return reviewReportStatus;
    }

    public void setReviewReportStatus(String reviewReportStatus) {
        this.reviewReportStatus = reviewReportStatus == null ? null : reviewReportStatus.trim();
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
        ManageReviewPlanEntity other = (ManageReviewPlanEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getPlanCreator() == null ? other.getPlanCreator() == null : this.getPlanCreator().equals(other.getPlanCreator()))
                && (this.getPlanCreationTime() == null ? other.getPlanCreationTime() == null : this.getPlanCreationTime().equals(other.getPlanCreationTime()))
                && (this.getReviewStartTime() == null ? other.getReviewStartTime() == null : this.getReviewStartTime().equals(other.getReviewStartTime()))
                && (this.getReviewEndTime() == null ? other.getReviewEndTime() == null : this.getReviewEndTime().equals(other.getReviewEndTime()))
                && (this.getReviewPurpose() == null ? other.getReviewPurpose() == null : this.getReviewPurpose().equals(other.getReviewPurpose()))
                && (this.getReviewHost() == null ? other.getReviewHost() == null : this.getReviewHost().equals(other.getReviewHost()))
                && (this.getReviewLocation() == null ? other.getReviewLocation() == null : this.getReviewLocation().equals(other.getReviewLocation()))
                && (this.getReviewReportStatus() == null ? other.getReviewReportStatus() == null : this.getReviewReportStatus().equals(other.getReviewReportStatus()))
                && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPlanCreator() == null) ? 0 : getPlanCreator().hashCode());
        result = prime * result + ((getPlanCreationTime() == null) ? 0 : getPlanCreationTime().hashCode());
        result = prime * result + ((getReviewStartTime() == null) ? 0 : getReviewStartTime().hashCode());
        result = prime * result + ((getReviewEndTime() == null) ? 0 : getReviewEndTime().hashCode());
        result = prime * result + ((getReviewPurpose() == null) ? 0 : getReviewPurpose().hashCode());
        result = prime * result + ((getReviewHost() == null) ? 0 : getReviewHost().hashCode());
        result = prime * result + ((getReviewLocation() == null) ? 0 : getReviewLocation().hashCode());
        result = prime * result + ((getReviewReportStatus() == null) ? 0 : getReviewReportStatus().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }
}