package com.jifen.manage.demo.entity;

import lombok.Data;

import java.util.List;

/**
 * 登录验证成功返回
 *
 * @author gjl
 */


@Data
public class LoginToken {
    /**
     * 管理员姓名
     */
    private String name;
    private String token;
    private String adminId;
    private String passWord;
    /**
     * 账号
     */
    private String nick;
    /**
     * 菜单
     */
    private List<FunctionEntity> list;

    private UserInfo userInfo;

}
