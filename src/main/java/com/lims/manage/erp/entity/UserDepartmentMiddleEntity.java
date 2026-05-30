package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("sys_user_department_middle")
public class UserDepartmentMiddleEntity implements Serializable {
    private Long userId;

    private String dingUserId;

    private Long deptId;

//    private static final long serialVersionUID = 1L;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDingUserId() {
        return dingUserId;
    }

    public void setDingUserId(String dingUserId) {
        this.dingUserId = dingUserId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
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
        UserDepartmentMiddleEntity other = (UserDepartmentMiddleEntity) that;
        return (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getDingUserId() == null ? other.getDingUserId() == null : this.getDingUserId().equals(other.getDingUserId()))
                && (this.getDeptId() == null ? other.getDeptId() == null : this.getDeptId().equals(other.getDeptId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getDingUserId() == null) ? 0 : getDingUserId().hashCode());
        result = prime * result + ((getDeptId() == null) ? 0 : getDeptId().hashCode());
        return result;
    }
}