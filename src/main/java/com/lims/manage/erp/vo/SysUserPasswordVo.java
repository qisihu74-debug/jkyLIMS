package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2022/2/7 14:44
 * 前端JSON格式 方便后台接口接收
 */
@Data
public class SysUserPasswordVo {
    /**
     * 账号
     */
    private String username;
    /**
     * 旧密码
     */
    private String oldPassword;
    /**
     * 新密码
     */
    private String newPassword;
}
