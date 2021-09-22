package com.stu.manage.demo.service;

import com.stu.manage.demo.entity.Login;
import com.stu.manage.demo.entity.LoginToken;

import java.util.List;

/**
 * @author gjl
 */

public interface LoginService {

    /**
     * 查询管理员对应的密码
     *
     * @param nick
     * @return String
     */
    Login getAdmin(String nick);

    /**
     * 获取用户对象
     * @param adminId
     * @return
     */
    LoginToken getUser(String adminId);

    /**
     * 注册账号
     * @param login
     */
    int save(Login login);

    List<Login> adminList();
}
