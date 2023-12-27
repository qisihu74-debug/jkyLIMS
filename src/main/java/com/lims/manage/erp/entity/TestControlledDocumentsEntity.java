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
@TableName("test_controlled_documents")
public class TestControlledDocumentsEntity implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String documentsCode;

    private String documentsName;

    private String isAvailable;

    private String status;

    private Integer delFlag;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date usageTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expirationTime;

    private String remark;

    private Integer pid;

    private Integer fileType;

    private String fileTypeContent;

    private String documentsFileUri;

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


    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDocumentsCode() {
        return documentsCode;
    }

    public void setDocumentsCode(String documentsCode) {
        this.documentsCode = documentsCode == null ? null : documentsCode.trim();
    }

    public String getDocumentsName() {
        return documentsName;
    }

    public void setDocumentsName(String documentsName) {
        this.documentsName = documentsName == null ? null : documentsName.trim();
    }

    public String getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(String isAvailable) {
        this.isAvailable = isAvailable == null ? null : isAvailable.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
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

    public Date getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(Date usageTime) {
        this.usageTime = usageTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public String getFileTypeContent() {
        return fileTypeContent;
    }

    public void setFileTypeContent(String fileTypeContent) {
        this.fileTypeContent = fileTypeContent == null ? null : fileTypeContent.trim();
    }

    public String getDocumentsFileUri() {
        return documentsFileUri;
    }

    public void setDocumentsFileUri(String documentsFileUri) {
        this.documentsFileUri = documentsFileUri == null ? null : documentsFileUri.trim();
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
        TestControlledDocumentsEntity other = (TestControlledDocumentsEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getDocumentsCode() == null ? other.getDocumentsCode() == null : this.getDocumentsCode().equals(other.getDocumentsCode()))
            && (this.getDocumentsName() == null ? other.getDocumentsName() == null : this.getDocumentsName().equals(other.getDocumentsName()))
            && (this.getIsAvailable() == null ? other.getIsAvailable() == null : this.getIsAvailable().equals(other.getIsAvailable()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getUsageTime() == null ? other.getUsageTime() == null : this.getUsageTime().equals(other.getUsageTime()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getPid() == null ? other.getPid() == null : this.getPid().equals(other.getPid()))
            && (this.getFileType() == null ? other.getFileType() == null : this.getFileType().equals(other.getFileType()))
            && (this.getFileTypeContent() == null ? other.getFileTypeContent() == null : this.getFileTypeContent().equals(other.getFileTypeContent()))
            && (this.getDocumentsFileUri() == null ? other.getDocumentsFileUri() == null : this.getDocumentsFileUri().equals(other.getDocumentsFileUri()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getDocumentsCode() == null) ? 0 : getDocumentsCode().hashCode());
        result = prime * result + ((getDocumentsName() == null) ? 0 : getDocumentsName().hashCode());
        result = prime * result + ((getIsAvailable() == null) ? 0 : getIsAvailable().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getUsageTime() == null) ? 0 : getUsageTime().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getPid() == null) ? 0 : getPid().hashCode());
        result = prime * result + ((getFileType() == null) ? 0 : getFileType().hashCode());
        result = prime * result + ((getFileTypeContent() == null) ? 0 : getFileTypeContent().hashCode());
        result = prime * result + ((getDocumentsFileUri() == null) ? 0 : getDocumentsFileUri().hashCode());
        return result;
    }
}