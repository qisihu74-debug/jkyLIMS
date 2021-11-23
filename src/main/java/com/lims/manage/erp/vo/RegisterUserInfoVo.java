package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class RegisterUserInfoVo {
    private String name;
    private String username;
    private String mobile;
    private String password;
    private List<Long> roleIds;
    private String deptId;
    private String email;
    private String state;
    private String note;
}
