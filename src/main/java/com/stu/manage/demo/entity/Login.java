package com.stu.manage.demo.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 登录
 *
 * @author gjl
 */


@Data
@TableName(value = "jky_ding_lims_user")
public class Login {
    private Long id;
    /**
     * 用户名称
     */
    private String adminName;
    private String passWord;
    private String adminId;
    /**
     * 账号
     */
    private String nick;
    private String mobile;
    private String address;
}
