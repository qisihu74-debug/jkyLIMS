package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lims.manage.erp.vo.RegisterUserInfoVo;
import lombok.Data;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2021/11/19 10:42
 * @Copyright © 河南交科院
 */
@Data
@TableName("sys_ding_user")
public class DingUserEntity {
    private int active;
    private String avatar;
    private String department;
    private String dingid;
    private String email;
    private String extattr;
    private Date Hireddate;
    private int isadmin;
    private int isboss;
    private int ishide;
    private int isleader;
    private String jobnumber;
    private String mobile;
    private String name;
    private Long orders;
    private String orgemail;
    private String position;
    private String remark;
    private String tel;
    private String unionid;
    @TableId(value = "userid")
    private String userid;
    private String workplace;

    public DingUserEntity() {
    }

    public DingUserEntity(String userid,RegisterUserInfoVo vo) {
        this.userid = userid;
        this.name = vo.getName();
        this.mobile = vo.getMobile();
        this.email = vo.getEmail();
        this.department = vo.getDeptId();
    }
}
