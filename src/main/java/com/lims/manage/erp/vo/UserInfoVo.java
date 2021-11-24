package com.lims.manage.erp.vo;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class UserInfoVo {
    private Long userId;
    private String username;
    private Timestamp createTime;
    private String state;
    private String note;
    private String name;
    private String mobile;
    private String position;
    private String email;
    private String departmentId;
    private List<LabelValueVo> department;
    private List<LabelValueVo> roles;
}
