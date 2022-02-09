package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class RegisterUserInfoVo {
    /**
     * 中文名
     */
    private String name;
    /**
     * 登陆账号
     */
    private String username;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 密码
     */
    private String password;
    /**
     * 权限数组
     */
    private List<Long> roleIds;
    private String deptId;
    private String email;
    private String state;
    private String note;
}
