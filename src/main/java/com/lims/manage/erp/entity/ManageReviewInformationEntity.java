package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.vo.LabelValueVo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@SuppressWarnings("serial")
@TableName("manage_review_information")
public class ManageReviewInformationEntity implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String planCreator;

    private String duties;

    private String originalFileName;

    private String fileUrl;

    private Integer delFlag;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    private Integer pid;
    /**
     * url附件信息集合
     */
    @TableField(exist = false)
    private List<LabelValueVo> urls = new ArrayList<>();

    private static final long serialVersionUID = 1L;

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

    public String getDuties() {
        return duties;
    }

    public void setDuties(String duties) {
        this.duties = duties == null ? null : duties.trim();
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName == null ? null : originalFileName.trim();
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl == null ? null : fileUrl.trim();
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

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
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
        ManageReviewInformationEntity other = (ManageReviewInformationEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPlanCreator() == null ? other.getPlanCreator() == null : this.getPlanCreator().equals(other.getPlanCreator()))
            && (this.getDuties() == null ? other.getDuties() == null : this.getDuties().equals(other.getDuties()))
            && (this.getOriginalFileName() == null ? other.getOriginalFileName() == null : this.getOriginalFileName().equals(other.getOriginalFileName()))
            && (this.getFileUrl() == null ? other.getFileUrl() == null : this.getFileUrl().equals(other.getFileUrl()))
            && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getPid() == null ? other.getPid() == null : this.getPid().equals(other.getPid()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPlanCreator() == null) ? 0 : getPlanCreator().hashCode());
        result = prime * result + ((getDuties() == null) ? 0 : getDuties().hashCode());
        result = prime * result + ((getOriginalFileName() == null) ? 0 : getOriginalFileName().hashCode());
        result = prime * result + ((getFileUrl() == null) ? 0 : getFileUrl().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getPid() == null) ? 0 : getPid().hashCode());
        return result;
    }
}