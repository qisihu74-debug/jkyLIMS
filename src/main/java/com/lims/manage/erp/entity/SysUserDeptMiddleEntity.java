package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("sys_user_dept_middle")
public class SysUserDeptMiddleEntity implements Serializable {
    private Integer id;

    private String userId;

    private Long deptId;
}