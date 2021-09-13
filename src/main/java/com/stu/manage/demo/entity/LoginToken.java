package com.stu.manage.demo.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 登录验证成功返回
 *
 * @author gjl
 */


@Data
public class LoginToken {
    @NotNull
    private String name;
    private String passWord;
    private String token;
    private String adminId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
}
