package com.stu.manage.demo.entity;

import com.sun.istack.internal.NotNull;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc lims系统委托接口参数之一，本系统登录后前端保存
 * @date 2021/9/22 10:30
 * @Copyright © 河南交科院
 */
@Data
public class UserInfo {
    private Integer userId;
    private String userName;
    private String idType;
    private String idNo;
    private String loginNo;
    private String tel;
    private String sex;
    private String address;

    private String signatureId;

    private Integer signatureFileId;

    @NotNull
    private Integer depId;
    private String depName;
    //private List<RoleCode> roleCodeList;
}
