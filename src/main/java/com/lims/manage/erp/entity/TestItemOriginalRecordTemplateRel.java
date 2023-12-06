package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
@Data
public class TestItemOriginalRecordTemplateRel implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer checkItemId;

    private Integer originalRecordTemplateId;

    private static final long serialVersionUID = 1L;

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

    public Integer getOriginalRecordTemplateId() {
        return originalRecordTemplateId;
    }

    public void setOriginalRecordTemplateId(Integer originalRecordTemplateId) {
        this.originalRecordTemplateId = originalRecordTemplateId;
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
        TestItemOriginalRecordTemplateRel other = (TestItemOriginalRecordTemplateRel) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getCheckItemId() == null ? other.getCheckItemId() == null : this.getCheckItemId().equals(other.getCheckItemId()))
            && (this.getOriginalRecordTemplateId() == null ? other.getOriginalRecordTemplateId() == null : this.getOriginalRecordTemplateId().equals(other.getOriginalRecordTemplateId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCheckItemId() == null) ? 0 : getCheckItemId().hashCode());
        result = prime * result + ((getOriginalRecordTemplateId() == null) ? 0 : getOriginalRecordTemplateId().hashCode());
        return result;
    }
}